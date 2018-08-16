package me.kosert.youtubeplayer.network.requests

import me.kosert.youtubeplayer.network.RequestMethod
import me.kosert.youtubeplayer.network.responses.AbstractResponseMessage

abstract class AbstractRequestMessage
{
	abstract val method : RequestMethod
	abstract val path : String
	abstract val responseClass : Class<out AbstractResponseMessage>

	abstract override fun toString() : String

	open fun getParams() : MutableMap<String, String> = mutableMapOf()
}