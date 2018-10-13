package me.kosert.youtubeplayer.music

import me.kosert.youtubeplayer.service.PlayingState
import me.kosert.youtubeplayer.service.Song

class StateEvent(
		val state: PlayingState,
		val song: Song?,
		val millis: Int
)