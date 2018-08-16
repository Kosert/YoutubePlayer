package me.kosert.youtubeplayer.ui.activities.player

import android.os.Bundle
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.activity_player.*
import me.kosert.youtubeplayer.R
import me.kosert.youtubeplayer.music.PlayingStateEvent
import me.kosert.youtubeplayer.ui.activities.AbstractActivity

class PlayerActivity : AbstractActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        setSupportActionBar(toolbar)

        //TODO
        // buttons play/pause stop skip
        // recycler songs
        // recycler item -> song number, title, length? moveable?


    }

    @Subscribe
    fun onPlayerStateChanged(event: PlayingStateEvent) {

    }
}