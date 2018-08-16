package me.kosert.youtubeplayer.ui.activities.main.webview

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import me.kosert.youtubeplayer.util.Logger

class WebClient : WebViewClient() {

    private val logger = Logger("WebClient")

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return false
    }
}
