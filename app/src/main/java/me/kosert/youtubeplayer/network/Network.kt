package me.kosert.youtubeplayer.network

import com.android.volley.Response
import com.android.volley.toolbox.Volley
import me.kosert.youtubeplayer.App
import me.kosert.youtubeplayer.Conf
import me.kosert.youtubeplayer.GlobalProvider
import me.kosert.youtubeplayer.network.requests.AbstractRequestMessage
import me.kosert.youtubeplayer.network.requests.ParametrizedStringRequest
import me.kosert.youtubeplayer.network.responses.AbstractResponseMessage
import me.kosert.youtubeplayer.util.Logger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


object Network
{
	private const val baseUrl = Conf.KOSERT_URL

	private val logger = Logger("Network")

	private val bus by lazy {
		GlobalProvider.bus
	}

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
			bus.post(NetworkResponseEvent(requestMessage, responseMessage))
		},
		Response.ErrorListener { error ->
			logger.e("Request failed $error for ${requestMessage::class.java.simpleName}")

            bus.post(NetworkResponseEvent(requestMessage))
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