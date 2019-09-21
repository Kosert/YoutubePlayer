package me.kosert.youtubeplayer.music

import android.app.DownloadManager
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import kotlinx.coroutines.*
import me.kosert.channelbus.GlobalBus
import me.kosert.youtubeplayer.App
import me.kosert.youtubeplayer.network.Network
import me.kosert.youtubeplayer.network.requests.GetInfoRequest
import me.kosert.youtubeplayer.network.responses.GetInfoResponse
import me.kosert.youtubeplayer.service.DownloadEvent
import me.kosert.youtubeplayer.service.Song
import java.util.concurrent.TimeUnit

object MusicProvider {

    fun isImageSaved(song: Song) = song.getImage() != null

    fun isSongSaved(song: Song): Boolean {
        return song.getMusicFile().exists()
    }

    fun checkQueue() {
        val notSaved = MusicQueue.queue.filterNot { isSongSaved(it) || it.isFromFile() }
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
                withContext(Dispatchers.Main) {
                    Toast.makeText(App.get(), "ERROR: No suitable audio format :(", Toast.LENGTH_LONG).show()
                }
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
            GlobalBus.post(DownloadEvent(song.ytUrl))
        }
    }

    fun newSongFromFile(uri: Uri, name: String) {

        CoroutineScope(Job() + Dispatchers.Default).launch {

            val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz-_123456789"
            val newId = (1..20).map { allowedChars.random() }.joinToString("")

            val duration = MediaMetadataRetriever().run {
                setDataSource(App.get(), uri)
                val millis = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
                TimeUnit.MILLISECONDS.toSeconds(millis).toInt()
            }

            val song = Song(name, "fakeUrl=$newId", duration, null)

            App.get().contentResolver.openInputStream(uri).use { input ->
                song.getMusicFile().outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            MusicQueue.addSong(song)
        }
    }
}