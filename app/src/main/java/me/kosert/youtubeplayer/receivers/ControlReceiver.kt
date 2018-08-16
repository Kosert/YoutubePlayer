package me.kosert.youtubeplayer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.kosert.youtubeplayer.GlobalProvider
import me.kosert.youtubeplayer.service.ControlEvent
import me.kosert.youtubeplayer.service.OperationType

class ControlReceiver : BroadcastReceiver() {

    val bus = GlobalProvider.bus

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            GlobalProvider.PLAY_ACTION -> {
                bus.post(ControlEvent(OperationType.PLAY))
            }
            GlobalProvider.PAUSE_ACTION -> {
                bus.post(ControlEvent(OperationType.PAUSE))
            }
            GlobalProvider.STOP_ACTION -> {
                //TODO bus.post(ControlEvent(OperationType.STOP))
            }
        }
    }
}