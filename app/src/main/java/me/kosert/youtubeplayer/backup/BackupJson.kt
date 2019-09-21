package me.kosert.youtubeplayer.backup

import com.google.gson.annotations.Expose
import me.kosert.youtubeplayer.service.Song

class BackupJson(
        @Expose val playlists: Array<Playlist>
)

class Playlist(
        @Expose val name: String,
        @Expose val songs: Array<Song>
)