package me.kosert.youtubeplayer.webview

import android.webkit.WebChromeClient
import android.webkit.WebView
import me.kosert.youtubeplayer.network.Network
import me.kosert.youtubeplayer.network.requests.GetInfoRequest
import me.kosert.youtubeplayer.util.Logger

class ChromeClient(val webView: WebView) : WebChromeClient() {

    private val logger = Logger("ChromeClient")

    override fun onReceivedTitle(view: WebView, title: String?) {
        super.onReceivedTitle(view, title)

        logger.i("Page title: $title - ${view.url}")

        if (view.url.contains("watch?v=")) {
            view.stopLoading()

            val request = GetInfoRequest(view.url)
            Network.send(request)

            //val disableVideoScript = "document.getElementsByTagName('video')[0].src = ''"
            //view.evaluateJavascript(disableVideoScript, {})
        }


    }
}
