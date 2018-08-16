package me.kosert.youtubeplayer.network

import me.kosert.youtubeplayer.GlobalProvider
import me.kosert.youtubeplayer.network.responses.AbstractResponseMessage

class ResponseBuilder {

    private lateinit var clazz: Class<out AbstractResponseMessage>
    private lateinit var content: String

    fun setClass(responseClass: Class<out AbstractResponseMessage>): ResponseBuilder {
        clazz = responseClass
        return this
    }

    fun setContent(responseContent: String): ResponseBuilder {
        content = responseContent
        return this
    }

    fun create(): AbstractResponseMessage {
        return GlobalProvider.gson.fromJson<AbstractResponseMessage>(content, clazz)
    }

}