package me.kosert.youtubeplayer.music

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import kotlinx.coroutines.*
import me.kosert.youtubeplayer.App
import me.kosert.youtubeplayer.GlobalProvider.bus
import me.kosert.youtubeplayer.network.Network
import me.kosert.youtubeplayer.network.requests.GetInfoRequest
import me.kosert.youtubeplayer.network.responses.GetInfoResponse
import me.kosert.youtubeplayer.service.DownloadEvent
import me.kosert.youtubeplayer.service.Song

object MusicProvider {

    fun isImageSaved(song: Song) = song.getImage() != null

    fun isSongSaved(song: Song): Boolean {
        return song.getMusicFile().exists()
    }

    fun checkQueue() {
        val notSaved = MusicQueue.queue.filterNot { isSongSaved(it) }
        val toDownload = notSaved.take(5)

        Toast.makeText(App.get(), "Songs not downloaded: ${notSaved.size}, Song scheduled for download: ${toDownload.size}", Toast.LENGTH_SHORT).show()
        toDownload.forEach { fetchSong(it) }
    }

    val downloading = mutableMapOf<Long, String>()

    fun fetchSong(song: Song) {

        if (downloading.containsValue(song.ytUrl)) return

        CoroutineScope(Job() + Dispatchers.Default).launch {
            val format = song.format ?: run {
                val request = GetInfoRequest(song.ytUrl)
                val response = Network.sendSuspend(request) as GetInfoResponse? ?: return@run null

                if (response.formats == null) return@run null

                val audioFormats = response.formats.filter {
                    it.audioEncoding != null && it.audioEncoding != "opus" && it.encoding == null
                }.sortedByDescending { it.audioBitrate?.toInt() }

                audioFormats.firstOrNull()
            }
            if (format == null) {
                Crashlytics.logException(IllegalArgumentException("No valid format for video: ${song.ytUrl}"))
                Toast.makeText(App.get(), "ERROR: No suitable audio format :(", Toast.LENGTH_LONG).show()
                return@launch
            }

            val request = DownloadManager.Request(Uri.parse(format.url)).apply {
                setTitle(song.title)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                setDestinationUri(Uri.fromFile(song.getMusicFile()))
            }

            val manager = App.get().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val id = manager.enqueue(request)
            downloading[id] = song.ytUrl
            withContext(Dispatchers.Main) {
                bus.post(DownloadEvent(song.ytUrl))
            }
        }

    }
}