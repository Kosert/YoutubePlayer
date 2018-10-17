package me.kosert.youtubeplayer.network

import me.kosert.youtubeplayer.network.requests.AbstractRequestMessage
import me.kosert.youtubeplayer.network.responses.AbstractResponseMessage

class NetworkResponseEvent {

    val status: MessageStatus
    val requestMessage: AbstractRequestMessage
    val responseMessage: AbstractResponseMessage?

    constructor(requestMessage: AbstractRequestMessage) {
        this.status = MessageStatus.FAILED
        this.requestMessage = requestMessage
        this.responseMessage = null
    }

    constructor(requestMessage: AbstractRequestMessage, responseMessage: AbstractResponseMessage) {
        this.status = MessageStatus.RECEIVED
        this.requestMessage = requestMessage
        this.responseMessage = responseMessage
    }

}