package me.kosert.youtubeplayer.ui.activities.main.webview

import android.webkit.WebChromeClient
import android.webkit.WebView
import me.kosert.youtubeplayer.ui.activities.main.MainActivityCallbacks
import me.kosert.youtubeplayer.util.Logger

class ChromeClient(
        private val callbacks: MainActivityCallbacks
) : WebChromeClient() {

    private val logger = Logger("ChromeClient")
    private var lastUrl = ""

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)

        val newUrl = view.url
        if (newUrl != lastUrl) {
            lastUrl = newUrl
            logger.i("Url changed - ${view.url} ($newProgress)")

            if (newUrl.contains("watch?v=")) {
                view.stopLoading()

                callbacks.onVideoClicked()
            }

        }
    }
}
