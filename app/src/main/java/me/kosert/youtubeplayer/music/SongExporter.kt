package me.kosert.youtubeplayer.music

import android.os.Environment
import android.widget.Toast
import me.kosert.youtubeplayer.App
import me.kosert.youtubeplayer.appTag
import me.kosert.youtubeplayer.service.Song
import java.io.File

object SongExporter {

    private val folder by lazy {
        val storage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        File(storage, appTag)
    }

    //todo pojedyncze
    fun exportSong(song: Song) {
        val target = File(folder, "${song.title}.m4a")
        song.getMusicFile().copyTo(target)
    }

    fun exportQueue() {
        MusicQueue.queue.toList().forEach {
            exportSong(it)
        }
        Toast.makeText(App.get(), "Songs exported", Toast.LENGTH_SHORT).show()
    }
}