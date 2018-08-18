package me.kosert.youtubeplayer

import android.app.Application

const val appTag = "YoutubePlayer"

class App : Application() {

	init {
		instance = this
	}

	override fun onCreate() {
		super.onCreate()
	}

	companion object {
		private lateinit var instance : App

		fun get() = instance
	}
}