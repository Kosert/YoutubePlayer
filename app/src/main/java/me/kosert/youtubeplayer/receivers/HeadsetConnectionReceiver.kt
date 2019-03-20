package me.kosert.youtubeplayer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import me.kosert.channelbus.GlobalBus
import me.kosert.youtubeplayer.service.ControlEvent
import me.kosert.youtubeplayer.service.OperationType

class HeadsetConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == AudioManager.ACTION_HEADSET_PLUG) {
            val state = intent.getIntExtra("state", 0)
            if (state == 0)
                GlobalBus.post(ControlEvent(OperationType.PAUSE))
        }

    }

}