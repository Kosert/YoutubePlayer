package me.kosert.youtubeplayer.network.requests

import me.kosert.youtubeplayer.network.RequestMethod
import me.kosert.youtubeplayer.network.responses.GetInfoResponse

class GetInfoRequest(
        val url: String,
        val isFromActivity: Boolean = false
): AbstractRequestMessage() {

    override val method = RequestMethod.POST
    override val path = "/getInfo"
    override val responseClass = GetInfoResponse::class.java

    override fun getParams(): MutableMap<String, String> {
        return mutableMapOf(Pair("url", url))
    }

    override fun toString(): String {
        return "GetInfoRequest(method=$method, path='$path', responseClass=$responseClass)"
    }
}