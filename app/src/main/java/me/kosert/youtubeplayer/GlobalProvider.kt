package me.kosert.youtubeplayer

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.kosert.channelbus.GlobalBus
import me.kosert.youtubeplayer.memory.AppData
import me.kosert.youtubeplayer.music.MusicQueue
import me.kosert.youtubeplayer.music.StateEvent
import me.kosert.youtubeplayer.service.PlayingState

object GlobalProvider {

    const val SHUTDOWN_ACTION = "me.kosert.youtubeplayer.SHUTDOWN"
    const val PLAY_ACTION = "me.kosert.youtubeplayer.PLAY"
    const val PAUSE_ACTION = "me.kosert.youtubeplayer.PAUSE"
    const val STOP_ACTION = "me.kosert.youtubeplayer.STOP"
    const val NEXT_ACTION = "me.kosert.youtubeplayer.NEXT"

    val gson: Gson by lazy {
        GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
    }

    val currentState
        get() = GlobalBus.getLastEvent(StateEvent::class.java)
                ?: StateEvent(PlayingState.STOPPED,
                        MusicQueue.queue.getOrNull(AppData.getInt(AppData.IntType.CURRENT_POSITION)),
                        0
                )
}