package me.kosert.youtubeplayer.ui.activities.splash

import android.content.Intent
import me.kosert.youtubeplayer.ui.activities.AbstractActivity
import me.kosert.youtubeplayer.ui.activities.main.MainActivity


class SplashScreenActivity : AbstractActivity()
{

    override fun onStart()
    {
        super.onStart()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}