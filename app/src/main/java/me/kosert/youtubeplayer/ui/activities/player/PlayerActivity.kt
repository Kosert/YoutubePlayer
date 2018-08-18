package me.kosert.youtubeplayer.ui.activities.player

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.activity_player.*
import me.kosert.youtubeplayer.R
import me.kosert.youtubeplayer.music.MusicQueue
import me.kosert.youtubeplayer.music.PlayingStateEvent
import me.kosert.youtubeplayer.music.QueueChangedEvent
import me.kosert.youtubeplayer.music.State
import me.kosert.youtubeplayer.service.ControlEvent
import me.kosert.youtubeplayer.service.CurrentTimeEvent
import me.kosert.youtubeplayer.service.OperationType
import me.kosert.youtubeplayer.ui.activities.AbstractActivity
import java.text.DecimalFormat

class PlayerActivity : AbstractActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        setSupportActionBar(toolbar)

        songRecycler.layoutManager = LinearLayoutManager(this)
        songRecycler.adapter = SongAdapter(MusicQueue.getFullQueue())

        updateView()
        updateCurrentTime(MusicQueue.currentTime)

        playPauseButton.setOnClickListener {
            val event =
            if (MusicQueue.currentState != State.PLAYING)
                ControlEvent(OperationType.PLAY)
            else
                ControlEvent(OperationType.PAUSE)
            bus.post(event)
        }
        stopButton.setOnClickListener { bus.post(ControlEvent(OperationType.STOP)) }
        nextButton.setOnClickListener { bus.post(ControlEvent(OperationType.NEXT)) }
        seekBar.setOnTouchListener { _, _ -> return@setOnTouchListener true }
    }

    private fun updateView() {

        if (MusicQueue.currentState == State.PLAYING)
            playPauseButton.setImageDrawable(getDrawable(R.drawable.ic_pause_black_24dp))
        else
            playPauseButton.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_black_24dp))

        val formatter = DecimalFormat("00")
        MusicQueue.getCurrent()?.let {
            val lengthString = "${it.length / 60}:${formatter.format(it.length % 60)}"
            lengthText.text = lengthString
            seekBar.max = it.length * 1000
        } ?: run {
            lengthText.text = "0:00"
        }
    }

    private fun updateCurrentTime(millis: Int) {
        val seconds = millis / 1000
        val formatter = DecimalFormat("00")
        val timeString = "${seconds / 60}:${formatter.format(seconds % 60)}"
        currentTimeText.text = timeString

        seekBar.progress = millis
    }

    @Subscribe
    fun onTimeEvent(event: CurrentTimeEvent) {
        updateCurrentTime(event.millis)
    }

    @Subscribe
    fun onQueueChanged(event: QueueChangedEvent) {
        songRecycler.adapter.notifyDataSetChanged()
    }

    @Subscribe
    fun onPlayerStateChanged(event: PlayingStateEvent) {
        handler.post {
            updateView()
            updateCurrentTime(MusicQueue.currentTime)
            songRecycler.adapter.notifyDataSetChanged()
        }
    }
}