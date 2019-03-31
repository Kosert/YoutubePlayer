package me.kosert.youtubeplayer.ui.activities.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import me.kosert.youtubeplayer.Conf
import me.kosert.youtubeplayer.R
import me.kosert.youtubeplayer.music.MusicQueue
import me.kosert.youtubeplayer.network.Network
import me.kosert.youtubeplayer.network.NetworkResponseEvent
import me.kosert.youtubeplayer.network.requests.GetInfoRequest
import me.kosert.youtubeplayer.network.responses.GetInfoResponse
import me.kosert.youtubeplayer.service.Song
import me.kosert.youtubeplayer.ui.activities.AbstractActivity
import me.kosert.youtubeplayer.ui.activities.main.webview.ChromeClient
import me.kosert.youtubeplayer.ui.activities.main.webview.WebClient

class MainActivity : AbstractActivity(), MainActivityCallbacks {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView.settings.mediaPlaybackRequiresUserGesture = true
        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = ChromeClient(this)
        webView.webViewClient = WebClient()
        webView.loadUrl(Conf.YOUTUBE_URL)
    }

    override fun onBackPressed() {
        if (webView.canGoBack())
            webView.goBack()
        else
            super.onBackPressed()
    }

    override fun onVideoClicked() {
        showProgress(true)
        val request = GetInfoRequest(webView.url, true)
        Network.send(request)
    }

    override fun onStart() {
        super.onStart()
        receiver.subscribe(true) { event: NetworkResponseEvent ->
            onNewNetworkEvent(event)
        }
    }

    private fun onNewNetworkEvent(event: NetworkResponseEvent) {

        val request = event.requestMessage as GetInfoRequest
        if (!request.isFromActivity) return

        val response = event.responseMessage as GetInfoResponse? ?: return

        showProgress(false)
        webView.goBack()

        if (response.formats == null) {
            showSnack("ERROR Cannot get video", Snackbar.LENGTH_LONG)
            return
        }

        val audioFormats = response.formats.filter {
            it.audioEncoding != null && it.audioEncoding != "opus" && it.encoding == null
        }.sortedByDescending { it.audioBitrate?.toInt() }

        if (audioFormats.isNotEmpty()) {
            val format = audioFormats[0]
            val song = Song(response.title, response.url, response.length, format)
            MusicQueue.addSong(song)
            showSnack("Added to queue: " + response.title, Snackbar.LENGTH_SHORT)
        } else {
            showSnack("ERROR: No suitable audio format :(", Snackbar.LENGTH_LONG)
        }
    }
}

