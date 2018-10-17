package me.kosert.youtubeplayer.music

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import me.kosert.youtubeplayer.App
import me.kosert.youtubeplayer.service.Song
import me.kosert.youtubeplayer.service.SongLoadedListener


object MusicProvider {

    fun isImageSaved(song: Song) = song.getImage() != null

    fun isSongSaved(song: Song) : Boolean {
        return song.getMusicFile().exists()
    }

    fun fetchSong(song: Song) {

//        song.downloadFormats(object : SongLoadedListener {
//            override fun onSongLoaded(uri: String) {
                val uri = song.format?.url

                val request = DownloadManager.Request(Uri.parse(uri))
                request.setDescription("Downloading: ${song.title}")
                request.setTitle("YouTubePlayer")
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                request.setDestinationUri(Uri.fromFile(song.getMusicFile()))

                val manager = App.get().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val id = manager.enqueue(request)
//                val receiver = MusicDownloadReceiver(id, Runnable {
//                    val downloadedUri = getSavedSong(song).path
//                    listener.onSongLoaded(downloadedUri)
//                })
//                App.get().registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
//            }
//        })

        //TODO("proxy the song and save")
    }
}