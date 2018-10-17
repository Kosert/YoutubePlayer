package me.kosert.youtubeplayer.music

import me.kosert.youtubeplayer.GlobalProvider.bus
import me.kosert.youtubeplayer.memory.AppData
import me.kosert.youtubeplayer.memory.AppData.AnyType.*
import me.kosert.youtubeplayer.memory.AppData.StringType.*
import me.kosert.youtubeplayer.service.ControlEvent
import me.kosert.youtubeplayer.service.OperationType
import me.kosert.youtubeplayer.service.PlayingState
import me.kosert.youtubeplayer.service.Song
import me.kosert.youtubeplayer.ui.dialogs.PlaylistItem
import me.kosert.youtubeplayer.util.Logger
import java.util.*


object MusicQueue {

    private val logger = Logger("MusicQueue")

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
        MusicProvider.fetchSong(song)
        AppData.setAny(USER_PLAYLIST, queue.toTypedArray())
        bus.post(QueueChangedEvent())
    }

    fun removeSong(position: Int) {

        val removed = queue.removeAt(position)
        AppData.setAny(USER_PLAYLIST, queue.toTypedArray())

        val all = queue.plus(AppData.getAny(SAVED_1) as Array<Song>)
                .plus(AppData.getAny(SAVED_2) as Array<Song>)
                .plus(AppData.getAny(SAVED_3) as Array<Song>)
                .distinctBy { it.ytUrl }

        if (all.none { it.ytUrl == removed.ytUrl }) {
            removed.getMusicFile().delete()
        }

        bus.post(QueueChangedEvent())
    }

    fun swap(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(MusicQueue.queue, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(MusicQueue.queue, i, i - 1)
            }
        }
        AppData.setAny(USER_PLAYLIST, queue.toTypedArray())
    }

    fun moveToPlaylist(index: Int, playlistItem: PlaylistItem) {
        val type = when (playlistItem.number) {
            1 -> SAVED_1
            2 -> SAVED_2
            3 -> SAVED_3
            else -> throw IllegalArgumentException()
        }
        val playlist = AppData.getAny(type) as Array<Song>
        AppData.setAny(type, playlist.plus(queue[index]))

        removeSong(index)
    }

    fun changePlaylist(number: Int) {
        bus.post(ControlEvent(OperationType.PLAYLIST_SWAP))

        logger.i("Swapped with playlist nr $number")

        val type = when (number) {
            1 -> SAVED_1
            2 -> SAVED_2
            3 -> SAVED_3
            else -> throw IllegalArgumentException()
        }
        val current = AppData.getAny(USER_PLAYLIST) as Array<*>
        val toSwap = AppData.getAny(type) as Array<*>
        AppData.setAny(type, current)
        AppData.setAny(USER_PLAYLIST, toSwap)

        val nameType = when (number) {
            1 -> SAVED_NAME_1
            2 -> SAVED_NAME_2
            3 -> SAVED_NAME_3
            else -> throw IllegalArgumentException()
        }
        val currentName = AppData.getString(USER_PLAYLIST_NAME)
        val toSwapName = AppData.getString(nameType)
        AppData.setString(nameType, currentName)
        AppData.setString(USER_PLAYLIST_NAME, toSwapName)

        queue.clear()
        toSwap.forEach { queue.add(it as Song) }
        bus.post(QueueChangedEvent())
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