package me.kosert.youtubeplayer.music

import me.kosert.youtubeplayer.GlobalProvider.bus
import me.kosert.youtubeplayer.memory.AppData
import me.kosert.youtubeplayer.memory.AppData.AnyType.*
import me.kosert.youtubeplayer.service.Song
import java.util.*


object MusicQueue {

    val queue by lazy {
        val array = AppData.getAny(USER_PLAYLIST) as Array<*>
        val queue = mutableListOf<Song>()
        array.forEach { queue.add(it as Song) }
        queue
    }

    fun getIndex(song: Song): Int {
        return queue.indexOfFirst { it.ytUrl == song.ytUrl }
    }

    fun addSong(song: Song) {
        queue.add(song)
        MusicProvider.fetchSongUri(song)
        AppData.setAny(USER_PLAYLIST, queue.toTypedArray())
        bus.post(QueueChangedEvent())
    }

    fun removeSong(position: Int) {
        queue.removeAt(position)
//        if (pointer > position)
//            pointer--
        AppData.setAny(USER_PLAYLIST, queue.toTypedArray())
        bus.post(QueueChangedEvent())
    }

    fun swap(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(MusicQueue.queue, i, i + 1)
            }
        }
        else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(MusicQueue.queue, i, i - 1)
            }
        }
        AppData.setAny(USER_PLAYLIST, queue.toTypedArray())
    }
}

/*
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
            bus.post(StateEvent(currentState))
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

    fun getNext(): Song? {
        return queue.getOrNull(pointer + 1)
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
}*/