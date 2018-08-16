package me.kosert.youtubeplayer

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.otto.Bus

object GlobalProvider {

    val bus by lazy {
        Bus()
    }

    val gson: Gson by lazy {
        GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
    }


}