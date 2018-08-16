package me.kosert.youtubeplayer.network

import me.kosert.youtubeplayer.network.responses.AbstractResponseMessage

class NetworkResponseEvent {

    val status: MessageStatus
    val responseMessage: AbstractResponseMessage?

    constructor() {
        status = MessageStatus.FAILED
        this.responseMessage = null
    }

    constructor(responseMessage: AbstractResponseMessage) {
        this.status = MessageStatus.RECEIVED
        this.responseMessage = responseMessage
    }

}