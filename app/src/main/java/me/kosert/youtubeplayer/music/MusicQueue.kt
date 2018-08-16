package me.kosert.youtubeplayer.music

import me.kosert.youtubeplayer.GlobalProvider
import me.kosert.youtubeplayer.service.ControlEvent
import me.kosert.youtubeplayer.service.OperationType
import me.kosert.youtubeplayer.service.Song

object MusicQueue {

    private val bus = GlobalProvider.bus
    private val queue = mutableListOf<Song>()

    var currentState: State = State.STOPPED
        private set(value) {
            field = value
            bus.post(PlayingStateEvent(currentState))
        }

    fun addSong(song: Song) {
        queue.add(song)
        if (queue.size == 1 && currentState != State.PLAYING) {
            bus.post(ControlEvent(OperationType.PLAY))
        }
    }

    fun getFirst() : Song? {
        return queue.firstOrNull()
    }

    fun onFinishedPlaying() {
        currentState = State.STOPPED
        queue.drop(1)
    }

    fun onPaused() {
        currentState = State.PAUSED
    }

    fun onStopped() {
        currentState = State.STOPPED
    }

    fun onStarted() {
        currentState = State.PLAYING
    }
}

enum class State {
    PLAYING,
    PAUSED,
    STOPPED
}