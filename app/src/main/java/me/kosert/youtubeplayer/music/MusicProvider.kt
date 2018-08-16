package me.kosert.youtubeplayer.music

import me.kosert.youtubeplayer.service.Song

object MusicProvider {

    fun isSongSaved(song: Song) : Boolean {
        return false
        TODO()
    }

    fun getSavedSong() {
        TODO()
    }

    fun getSongUri(song: Song) : String {
        return song.format.url
        TODO()
    }
}