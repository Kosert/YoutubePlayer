package me.kosert.youtubeplayer.music

import me.kosert.youtubeplayer.service.PlayingState
import me.kosert.youtubeplayer.service.Song

class StateEvent(
		val state: PlayingState,
		val song: Song?,
		val millis: Int
) {

	override fun toString(): String {
		return "StateEvent(state=$state, song=$song, millis=$millis)"
	}
}