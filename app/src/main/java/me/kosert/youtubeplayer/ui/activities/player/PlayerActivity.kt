package me.kosert.youtubeplayer.ui.activities.player

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.activity_player.*
import me.kosert.youtubeplayer.GlobalProvider
import me.kosert.youtubeplayer.R
import me.kosert.youtubeplayer.memory.AppData
import me.kosert.youtubeplayer.music.MusicQueue
import me.kosert.youtubeplayer.music.QueueChangedEvent
import me.kosert.youtubeplayer.music.StateEvent
import me.kosert.youtubeplayer.service.ControlEvent
import me.kosert.youtubeplayer.service.OperationType
import me.kosert.youtubeplayer.service.PlayingState
import me.kosert.youtubeplayer.service.Song
import me.kosert.youtubeplayer.ui.activities.AbstractActivity
import me.kosert.youtubeplayer.ui.dialogs.PlaylistsDialog
import java.text.DecimalFormat

class PlayerActivity : AbstractActivity(), PlayerView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        setSupportActionBar(toolbar)
        updateTitle()

        songRecycler.layoutManager = LinearLayoutManager(this)
        songRecycler.adapter = SongAdapter(this, supportFragmentManager, MusicQueue.queue)

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {

            override fun isItemViewSwipeEnabled(): Boolean = false
            override fun isLongPressDragEnabled(): Boolean = true

            override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
                return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
            }

            override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                MusicQueue.swap(viewHolder.adapterPosition, target.adapterPosition)
                songRecycler.adapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?) {
                super.clearView(recyclerView, viewHolder)
                bus.post(QueueChangedEvent())
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                throw(Exception("should not hit this"))
            }
        })
        touchHelper.attachToRecyclerView(songRecycler)

        playPauseButton.setOnClickListener {
            val event = if (GlobalProvider.currentState.state != PlayingState.PLAYING)
                ControlEvent(OperationType.PLAY)
            else
                ControlEvent(OperationType.PAUSE)
            bus.post(event)
        }
        stopButton.setOnClickListener { bus.post(ControlEvent(OperationType.STOP)) }
        nextButton.setOnClickListener { bus.post(ControlEvent(OperationType.NEXT)) }
        seekBar.setOnTouchListener { _, _ -> return@setOnTouchListener true }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.player_menu, menu)
        return true
    }

    private fun updateTitle() {
        val currentName = AppData.getString(AppData.StringType.USER_PLAYLIST_NAME)
        supportActionBar?.title = "YoutubePlayer ($currentName)"
    }

    private fun updateView(state: PlayingState) {
        if (state == PlayingState.PLAYING) //TODO animate
            playPauseButton.setImageDrawable(getDrawable(R.drawable.ic_pause_black_24dp))
        else
            playPauseButton.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_black_24dp))
    }

    private fun updateCurrentSong(song: Song?) {
        val formatter = DecimalFormat("00")
        song?.let {
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

    override fun onSongSelected(position: Int) {
        bus.post(ControlEvent(OperationType.SELECTED, position))
    }

    override fun onSongRemoveClicked(position: Int) {
        MusicQueue.removeSong(position)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId) {
            R.id.playlistButton -> {
                PlaylistsDialog.newInstance("Change playlist").apply {
                    onSelectedAction = { MusicQueue.changePlaylist(it.number) }
                }.show(supportFragmentManager, PlaylistsDialog.TAG)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    @Subscribe
    fun onQueueChanged(event: QueueChangedEvent) {
        songRecycler.adapter.notifyDataSetChanged()
        updateTitle()
    }

    private var previousState : StateEvent? = null

    @Subscribe
    fun onPlayerStateChanged(event: StateEvent) {
        handler.post {
            var songChanged = false

            if (previousState?.song !== event.song) {
                songChanged = true
                updateCurrentSong(event.song)
                val index1 = event.song?.let { MusicQueue.getIndex(it) }
                val index2 = previousState?.song?.let { MusicQueue.getIndex(it) }
                listOfNotNull(index1, index2).forEach {
                    songRecycler.adapter.notifyItemChanged(it)
                }
            }

            if (previousState?.state != event.state) {
                updateView(event.state)
                if (!songChanged)
                (event.song ?: previousState?.song)?.let {
                    val index = MusicQueue.getIndex(it)
                    songRecycler.adapter.notifyItemChanged(index)
                }
            }

            updateCurrentTime(event.millis)
            previousState = event
        }
    }
}