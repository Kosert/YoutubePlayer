package me.kosert.youtubeplayer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.kosert.channelbus.GlobalBus
import me.kosert.youtubeplayer.GlobalProvider
import me.kosert.youtubeplayer.service.ControlEvent
import me.kosert.youtubeplayer.service.OperationType

class ControlReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            GlobalProvider.PLAY_ACTION -> {
                GlobalBus.post(ControlEvent(OperationType.PLAY))
            }
            GlobalProvider.PAUSE_ACTION -> {
                GlobalBus.post(ControlEvent(OperationType.PAUSE))
            }
            GlobalProvider.STOP_ACTION -> {
                GlobalBus.post(ControlEvent(OperationType.STOP))
            }
            GlobalProvider.NEXT_ACTION -> {
                GlobalBus.post(ControlEvent(OperationType.NEXT))
            }
        }
    }
}