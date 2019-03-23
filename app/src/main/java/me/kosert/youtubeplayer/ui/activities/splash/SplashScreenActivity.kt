package me.kosert.youtubeplayer.ui.activities.splash

import android.content.Intent
import android.os.Build
import me.kosert.youtubeplayer.service.PlayerService
import me.kosert.youtubeplayer.ui.activities.AbstractActivity
import me.kosert.youtubeplayer.ui.activities.player.PlayerActivity


class SplashScreenActivity : AbstractActivity() {

    override fun onStart() {
        super.onStart()

        val serviceIntent = Intent(this, PlayerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(serviceIntent)
        else
            startService(serviceIntent)


        val intent = Intent(this, PlayerActivity::class.java)
        startActivity(intent)
    }
}