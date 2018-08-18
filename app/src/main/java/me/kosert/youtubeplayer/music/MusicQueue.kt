package me.kosert.youtubeplayer.music

import me.kosert.youtubeplayer.GlobalProvider
import me.kosert.youtubeplayer.memory.AppData
import me.kosert.youtubeplayer.memory.AppData.AnyType.USER_PLAYLIST
import me.kosert.youtubeplayer.service.ControlEvent
import me.kosert.youtubeplayer.service.CurrentTimeEvent
import me.kosert.youtubeplayer.service.OperationType
import me.kosert.youtubeplayer.service.Song

object MusicQueue {

    private val bus = GlobalProvider.bus
    private var pointer = 0
    private var enableLooping = false
    private val queue by lazy {
        val array = AppData.getAny(USER_PLAYLIST) as Array<*>
        val queue = mutableListOf<Song>()
        array.forEach { queue.add(it as Song) }
        queue
    }

    var currentTime = 0 // millis
        set(value) {
            field = value
            bus.post(CurrentTimeEvent(currentTime))
        }
    var currentState: State = State.STOPPED
        private set(value) {
            field = value
            bus.post(PlayingStateEvent(currentState))
        }

    fun uninit() {
        currentTime = 0
        currentState = State.STOPPED
    }

    fun getFullQueue(): List<Song> {
        return queue
    }

    fun addSong(song: Song) {
        queue.add(song)
        if (queue.size == 1 && currentState != State.PLAYING) {
            bus.post(ControlEvent(OperationType.PLAY))
        }
        AppData.setAny(USER_PLAYLIST, queue.toTypedArray())
        bus.post(QueueChangedEvent())
    }

    fun removeSong(position: Int) {
        queue.removeAt(position)
        if (pointer > position)
            pointer--
        AppData.setAny(USER_PLAYLIST, queue.toTypedArray())
        bus.post(QueueChangedEvent())
    }

    fun getCurrent(): Song? {
        return queue.getOrNull(pointer)
    }

    fun onFinishedPlaying() {
        pointer++
        if (enableLooping && pointer == queue.size)
            pointer = 0
        onStopped()
    }

    fun canPlayNext(): Boolean {
        return pointer != queue.size
    }

    fun onPaused() {
        currentState = State.PAUSED
    }

    fun onStopped() {
        currentTime = 0
        currentState = State.STOPPED
    }

    fun onStarted() {
        currentState = State.PLAYING
    }

    fun onSongSelected(position: Int) {
        bus.post(ControlEvent(OperationType.STOP))
        pointer = position
        bus.post(ControlEvent(OperationType.PLAY))
    }

    fun onNext() {
        onSongSelected(pointer + 1)
    }
}

enum class State {
    PLAYING,
    PAUSED,
    STOPPED
}