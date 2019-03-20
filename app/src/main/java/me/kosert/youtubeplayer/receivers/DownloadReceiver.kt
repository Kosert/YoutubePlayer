package me.kosert.youtubeplayer.receivers

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.kosert.channelbus.GlobalBus
import me.kosert.youtubeplayer.music.MusicProvider
import me.kosert.youtubeplayer.service.DownloadEvent

class DownloadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
        if (id == -1L) return

        MusicProvider.downloading.remove(id)?.let {
            GlobalBus.post(DownloadEvent(it))
        }
    }
}