package me.kosert.youtubeplayer.ui.activities.player

interface PlayerView {
    fun onSongSelected(position: Int)
    fun onSongRemoveClicked(position: Int)
}