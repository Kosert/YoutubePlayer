package me.kosert.youtubeplayer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.kosert.youtubeplayer.GlobalProvider
import me.kosert.youtubeplayer.service.PlayerService


class AppShutdownReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val shutdownIntent = Intent(context, PlayerService::class.java)
        context.stopService(shutdownIntent)

        GlobalProvider.bus.post(ShutdownEvent())
    }
}