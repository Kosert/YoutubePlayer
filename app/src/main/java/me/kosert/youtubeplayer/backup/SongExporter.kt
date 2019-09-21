package me.kosert.youtubeplayer.backup

import android.os.Environment
import android.widget.Toast
import me.kosert.channelbus.GlobalBus
import me.kosert.youtubeplayer.App
import me.kosert.youtubeplayer.GlobalProvider
import me.kosert.youtubeplayer.appTag
import me.kosert.youtubeplayer.memory.AppData
import me.kosert.youtubeplayer.music.MusicQueue
import me.kosert.youtubeplayer.music.QueueChangedEvent
import me.kosert.youtubeplayer.service.Song
import java.io.File

object SongExporter {

    private const val JSON_FILENAME = "playlists.json"
    private val folder by lazy {
        val storage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        File(storage, appTag)
    }

    fun exportSong(song: Song) {
        val target = File(folder, "${song.title}.m4a")
        song.getMusicFile().copyTo(target, true)
    }

    fun exportQueue() {
        MusicQueue.queue.toList().forEach {
            exportSong(it)
        }
        Toast.makeText(App.get(), "Songs exported", Toast.LENGTH_SHORT).show()
    }

    fun backupAll() {

        fun generatePlaylist(namePrefs: AppData.StringType, listPrefs: AppData.AnyType): Playlist {
            val name = AppData.getString(namePrefs)
            val list = AppData.getAny(listPrefs) as Array<Song>

            list.forEach { song ->
                val filename = song.getMusicFile().name
                val target = File(folder, filename)
                if (song.getMusicFile().exists())
                    song.getMusicFile().copyTo(target, true)
            }
            return Playlist(name, list)
        }

        val playlists = listOf(
                AppData.StringType.USER_PLAYLIST_NAME to AppData.AnyType.USER_PLAYLIST,
                AppData.StringType.SAVED_NAME_1 to AppData.AnyType.SAVED_1,
                AppData.StringType.SAVED_NAME_2 to AppData.AnyType.SAVED_2,
                AppData.StringType.SAVED_NAME_3 to AppData.AnyType.SAVED_3
        ).map { generatePlaylist(it.first, it.second) }.toTypedArray()
        val json = GlobalProvider.gson.toJson(BackupJson(playlists))
        val jsonFile = File(folder, JSON_FILENAME)
        jsonFile.writeText(json)
        Toast.makeText(App.get(), "Backup complete", Toast.LENGTH_SHORT).show()
    }

    fun restoreBackup() {

        fun Playlist.save(namePrefs: AppData.StringType, listPrefs: AppData.AnyType) {
            AppData.setString(namePrefs, name)
            AppData.setAny(listPrefs, songs)

            songs.forEach { song ->
                val filename = song.getMusicFile().name
                val localFile = File(folder, filename)
                if (localFile.exists())
                    localFile.copyTo(song.getMusicFile(), true)
            }
        }

        val jsonFile = File(folder, JSON_FILENAME)
        val json = jsonFile.readText()
        val playlists = GlobalProvider.gson.fromJson(json, BackupJson::class.java).playlists
        val prefs = listOf(
                AppData.StringType.USER_PLAYLIST_NAME to AppData.AnyType.USER_PLAYLIST,
                AppData.StringType.SAVED_NAME_1 to AppData.AnyType.SAVED_1,
                AppData.StringType.SAVED_NAME_2 to AppData.AnyType.SAVED_2,
                AppData.StringType.SAVED_NAME_3 to AppData.AnyType.SAVED_3
        )

        repeat(4) {
            playlists[it].save(prefs[it].first, prefs[it].second)
        }

        MusicQueue.queue.clear()
        MusicQueue.queue.addAll(playlists.first().songs)
        GlobalBus.post(QueueChangedEvent())
        Toast.makeText(App.get(), "Backup restored", Toast.LENGTH_SHORT).show()
    }
}