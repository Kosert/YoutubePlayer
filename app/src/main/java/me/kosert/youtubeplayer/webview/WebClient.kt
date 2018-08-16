package me.kosert.youtubeplayer.webview

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import me.kosert.youtubeplayer.util.Logger

class WebClient(val webView: WebView) : WebViewClient() {

    private val logger = Logger("WebClient")

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return false
    }
}
