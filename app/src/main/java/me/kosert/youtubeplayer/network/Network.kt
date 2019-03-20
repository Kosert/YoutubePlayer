package me.kosert.youtubeplayer.network

import com.android.volley.Response
import com.android.volley.toolbox.Volley
import me.kosert.channelbus.GlobalBus
import me.kosert.youtubeplayer.App
import me.kosert.youtubeplayer.network.requests.AbstractRequestMessage
import me.kosert.youtubeplayer.network.requests.ParametrizedStringRequest
import me.kosert.youtubeplayer.network.responses.AbstractResponseMessage
import me.kosert.youtubeplayer.util.Logger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


object Network {

	private val logger = Logger("Network")

	private val requestQueue by lazy {
		Volley.newRequestQueue(App.get())
	}

	fun send(requestMessage: AbstractRequestMessage) {

		logger.i("Sending request: $requestMessage")

		val request = ParametrizedStringRequest(requestMessage,
		Response.Listener { response ->
			logger.i("Response received: ${requestMessage.responseClass.simpleName} for ${requestMessage::class.java.simpleName}")

            val responseMessage = ResponseBuilder()
                    .setClass(requestMessage.responseClass)
                    .setContent(response)
                    .create()
			GlobalBus.post(NetworkResponseEvent(requestMessage, responseMessage))
		},
		Response.ErrorListener { error ->
			logger.e("Request failed $error for ${requestMessage::class.java.simpleName}")

            GlobalBus.post(NetworkResponseEvent(requestMessage))
		})

		requestQueue.add(request)
	}

	suspend fun sendSuspend(requestMessage: AbstractRequestMessage): AbstractResponseMessage? {

		logger.i("Sending request: $requestMessage")

		return suspendCoroutine {
			val request = ParametrizedStringRequest(requestMessage,
					Response.Listener { response ->
						logger.i("Response received: ${requestMessage.responseClass.simpleName} for ${requestMessage::class.java.simpleName}")

						val responseMessage = ResponseBuilder()
								.setClass(requestMessage.responseClass)
								.setContent(response)
								.create()

						it.resume(responseMessage)
					},
					Response.ErrorListener { error ->
						logger.e("Request failed $error for ${requestMessage::class.java.simpleName}")

						it.resume(null)
					})

			requestQueue.add(request)
		}
	}
}