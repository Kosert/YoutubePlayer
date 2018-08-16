package me.kosert.youtubeplayer.music

import me.kosert.youtubeplayer.service.Song

object MusicProvider {

    fun isSongSaved(song: Song) : Boolean {
        //TODO is song saved
        return false
    }

    fun getSavedSong() {
        TODO("get saved song")
    }

    fun getSongUri(song: Song) : String {
        return song.format.url
        TODO("proxy the song and save")
    }
}