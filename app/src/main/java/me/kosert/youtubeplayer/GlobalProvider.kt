package me.kosert.youtubeplayer

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.otto.Bus
import com.squareup.otto.ThreadEnforcer

object GlobalProvider {

    const val SHUTDOWN_ACTION = "me.kosert.youtubeplayer.SHUTDOWN"
    const val PLAY_ACTION = "me.kosert.youtubeplayer.PLAY"
    const val PAUSE_ACTION = "me.kosert.youtubeplayer.PAUSE"
    const val STOP_ACTION = "me.kosert.youtubeplayer.STOP"

    //TODO HandlerThread map

    val bus by lazy {
        Bus(ThreadEnforcer.ANY)
    }

    val gson: Gson by lazy {
        GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
    }


}