package me.kosert.youtubeplayer.network.requests

import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import me.kosert.youtubeplayer.Conf

class ParametrizedStringRequest(
        private val requestMessage: AbstractRequestMessage,
        listener: Response.Listener<String>,
        errorListener: Response.ErrorListener
) : StringRequest(
        requestMessage.method.id,
        Conf.KOSERT_URL + requestMessage.path,
        listener,
        errorListener
) {

    override fun getParams(): MutableMap<String, String> {
        return requestMessage.getParams()
    }
}