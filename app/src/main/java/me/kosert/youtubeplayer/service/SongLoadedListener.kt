package me.kosert.youtubeplayer.service

interface SongLoadedListener {
    fun onSongLoaded(uri: String)
}