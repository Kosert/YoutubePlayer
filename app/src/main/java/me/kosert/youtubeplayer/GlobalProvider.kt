package me.kosert.youtubeplayer

import android.os.Handler
import android.os.HandlerThread
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.otto.Bus
import com.squareup.otto.ThreadEnforcer
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

    private val asyncHandlers = mutableMapOf<String, Handler>()

    fun getAsyncHandler(key: String): Handler {
        return asyncHandlers.getOrPut(key) {
            val h = HandlerThread(key)
            h.start()
            Handler(h.looper)
        }
    }

    val bus by lazy {
        Bus(ThreadEnforcer.ANY)
    }

    val gson: Gson by lazy {
        GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
    }

    var currentState : StateEvent = StateEvent(PlayingState.STOPPED,
            MusicQueue.queue.getOrNull(AppData.getInt(AppData.IntType.CURRENT_POSITION)),
            0)
        set(value) {
            field = value
            bus.post(value)
        }
}