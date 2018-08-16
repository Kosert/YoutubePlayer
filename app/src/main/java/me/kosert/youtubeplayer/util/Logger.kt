package me.kosert.youtubeplayer.util

import android.util.Log
import me.kosert.youtubeplayer.Conf.DEBUG
import me.kosert.youtubeplayer.appTag

@Suppress("ConstantConditionIf")
open class Logger
{
	protected val tag : String

	constructor() {
		tag = appTag
	}

	constructor(logTag : String) {
		tag = "$appTag.$logTag"
	}

	fun d(message: String) {
		if (!DEBUG) return
		Log.d(tag, message)
	}

	fun i(message: String) {
		if (!DEBUG) return
		Log.i(tag, message)
	}

	fun w(message: String) {
		if (!DEBUG) return
		Log.w(tag, message)
	}

	fun e(message: String) {
		if (!DEBUG) return
		Log.e(tag, message)
	}

	fun list(list: List<Any>) {
		if (!DEBUG) return
		list.forEachIndexed { index, any ->
			Log.d(tag, index.toString() + ": " + any.toString())
		}
	}

	companion object : Logger() {
		init {
			Log.d(tag, "DEBUG MODE IS " + if(DEBUG) "ON" else "OFF")
		}
	}
}