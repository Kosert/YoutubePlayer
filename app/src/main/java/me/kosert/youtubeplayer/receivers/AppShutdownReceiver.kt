package me.kosert.youtubeplayer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.kosert.youtubeplayer.service.PlayerService
import me.kosert.youtubeplayer.ui.activities.CloseActivity


class AppShutdownReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        CloseActivity.closeApp(context)
        val serviceIntent = Intent(context, PlayerService::class.java)
        context.stopService(serviceIntent)
    }
}
