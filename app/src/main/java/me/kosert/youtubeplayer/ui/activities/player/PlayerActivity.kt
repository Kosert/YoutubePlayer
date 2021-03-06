package me.kosert.youtubeplayer.ui.activities.player

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_player.*
import me.kosert.channelbus.GlobalBus
import me.kosert.youtubeplayer.GlobalProvider
import me.kosert.youtubeplayer.R
import me.kosert.youtubeplayer.backup.SongExporter
import me.kosert.youtubeplayer.memory.AppData
import me.kosert.youtubeplayer.music.*
import me.kosert.youtubeplayer.service.*
import me.kosert.youtubeplayer.ui.activities.AbstractActivity
import me.kosert.youtubeplayer.ui.activities.main.MainActivity
import me.kosert.youtubeplayer.ui.dialogs.EditTextDialog
import me.kosert.youtubeplayer.ui.dialogs.PlaylistsDialog
import me.kosert.youtubeplayer.util.toColor
import java.text.DecimalFormat


class PlayerActivity : AbstractActivity(), PlayerView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        setSupportActionBar(toolbar)
        updateTitle()
        toolbar.overflowIcon?.setColorFilter(R.color.white.toColor(), PorterDuff.Mode.SRC_ATOP)

        songRecycler.layoutManager = LinearLayoutManager(this)
        songRecycler.adapter = SongAdapter(this, supportFragmentManager, MusicQueue.queue)

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {

            override fun isItemViewSwipeEnabled(): Boolean = false
            override fun isLongPressDragEnabled(): Boolean = true

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
                return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                MusicQueue.swap(viewHolder.adapterPosition, target.adapterPosition)
                songRecycler.adapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                GlobalBus.post(QueueChangedEvent())
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                throw(Exception("should not hit this"))
            }
        })
        touchHelper.attachToRecyclerView(songRecycler)

        playPauseButton.setOnClickListener {

            val event = if (GlobalProvider.currentState.state != PlayingState.PLAYING)
                ControlEvent(OperationType.PLAY)
            else
                ControlEvent(OperationType.PAUSE)
            GlobalBus.post(event)
        }
        stopButton.setOnClickListener { GlobalBus.post(ControlEvent(OperationType.STOP)) }
        nextButton.setOnClickListener { GlobalBus.post(ControlEvent(OperationType.NEXT)) }
        seekBar.setOnTouchListener { _, _ -> return@setOnTouchListener true }

        GlobalProvider.currentState.song?.let {
            val index = (MusicQueue.getIndex(it) + 5) //fixme calculate how many items fit on screen
                    .coerceAtMost(MusicQueue.queue.size - 1)
            songRecycler.smoothScrollToPosition(index)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.player_menu, menu)
        return true
    }

    private fun updateTitle() {
        val currentName = AppData.getString(AppData.StringType.USER_PLAYLIST_NAME)
        supportActionBar?.title = currentName
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
            totalTimeText.text = lengthString
            seekBar.max = it.length * 1000
        } ?: run {
            totalTimeText.text = "0:00"
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
        GlobalBus.post(ControlEvent(OperationType.SELECTED, position))
    }

    override fun onSongRemoveClicked(position: Int) {
        MusicQueue.removeSong(position)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.youtubeButton -> {
                startActivity(Intent(this@PlayerActivity, MainActivity::class.java))
                true
            }
            R.id.playlistButton -> {
                PlaylistsDialog.newInstance("Change playlist").apply {
                    onSelectedAction = { MusicQueue.changePlaylist(it.number) }
                }.show(supportFragmentManager, PlaylistsDialog.TAG)
                true
            }
            R.id.importButton -> {
                importFile()
                true
            }
            R.id.redownloadButton -> {
                MusicProvider.checkQueue()
                true
            }
            R.id.export -> {
                SongExporter.exportQueue()
                true
            }
            R.id.backupAll -> {
                SongExporter.backupAll()
                true
            }
            R.id.restoreAll -> {
                SongExporter.restoreBackup()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        receiver.subscribe { event: QueueChangedEvent ->
            songRecycler.adapter?.notifyDataSetChanged()
            updateTitle()
        }.subscribe { event: DownloadEvent ->
            val index = MusicQueue.getIndex(Song("", event.ytUrl, 0, null))
            songRecycler.adapter.notifyItemChanged(index)
        }.subscribe { event: StateEvent ->
            onPlayerStateChanged(event)
        }
    }

    private var previousState: StateEvent? = null

    private fun onPlayerStateChanged(event: StateEvent) {
        var songChanged = false

        if (previousState == null || previousState?.song?.ytUrl != event.song?.ytUrl) {
            songChanged = true
            updateCurrentSong(event.song)
            val index1 = event.song?.let { MusicQueue.getIndex(it) }
            val index2 = previousState?.song?.let { MusicQueue.getIndex(it) }
            listOfNotNull(index1, index2).forEach {
                songRecycler.adapter?.notifyItemChanged(it)
            }
        }

        if (previousState == null || previousState?.state != event.state) {
            updateView(event.state)
            if (!songChanged)
                (event.song ?: previousState?.song)?.let {
                    val index = MusicQueue.getIndex(it)
                    songRecycler.adapter?.notifyItemChanged(index)
                }
        }

        updateCurrentTime(event.millis)
        previousState = event
    }

    companion object {
        const val musicFileRequestCode = 2137
    }

    private fun importFile() {
        startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
        }, 2137)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != musicFileRequestCode || resultCode != Activity.RESULT_OK || data == null)
            return

        EditTextDialog.newInstance("Song name", "").apply {
            onStringChosenAction = {
                val uri = data.data
                MusicProvider.newSongFromFile(uri, it)
            }
        }.show(supportFragmentManager)
    }
}