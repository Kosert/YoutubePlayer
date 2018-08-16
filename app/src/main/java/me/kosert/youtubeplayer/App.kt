package me.kosert.youtubeplayer

import android.app.Application
import android.content.Intent
import me.kosert.youtubeplayer.service.PlayerService

const val appTag = "YoutubePlayer"

class App : Application() {

	init {
		instance = this
	}

	override fun onCreate() {
		super.onCreate()

		val intent = Intent(this, PlayerService::class.java)
		startService(intent)
	}

	companion object {
		private lateinit var instance : App

		fun get() = instance
	}
}