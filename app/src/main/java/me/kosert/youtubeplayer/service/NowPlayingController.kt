package me.kosert.youtubeplayer.service

import me.kosert.youtubeplayer.memory.AppData
import me.kosert.youtubeplayer.memory.AppData.IntType.*
import me.kosert.youtubeplayer.music.MusicQueue.getIndex
import me.kosert.youtubeplayer.music.MusicQueue.queue

class NowPlayingController {


	var currentSong : Song? = queue.getOrNull(AppData.getInt(CURRENT_POSITION))
	private set

	fun goNext() {
        currentSong?.let {
            selectSong(getIndex(it) + 1)
        } ?: run {
            selectSong(0)
        }
	}

	fun getNext(): Song? {
        if (currentSong == null) return null

		val currentIndex = getIndex(currentSong!!)
		return queue.getOrNull(currentIndex + 1)
	}

    fun selectSong(position: Int) {
        currentSong = queue.getOrNull(position)
        AppData.setInt(CURRENT_POSITION, currentSong?.let { getIndex(it) } ?: 0)
    }

}