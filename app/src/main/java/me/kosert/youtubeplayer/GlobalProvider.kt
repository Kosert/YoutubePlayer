package me.kosert.youtubeplayer

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.otto.Bus

object GlobalProvider {

    const val SHUTDOWN_ACTION = "me.kosert.youtubeplayer.SHUTDOWN"
    const val PLAY_ACTION = "me.kosert.youtubeplayer.PLAY"
    const val PAUSE_ACTION = "me.kosert.youtubeplayer.PAUSE"
    const val STOP_ACTION = "me.kosert.youtubeplayer.STOP"



    val bus by lazy {
        Bus()
    }

    val gson: Gson by lazy {
        GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
    }


}