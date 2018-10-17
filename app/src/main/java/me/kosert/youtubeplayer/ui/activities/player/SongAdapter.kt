package me.kosert.youtubeplayer.ui.activities.player

import android.content.Context
import android.graphics.Typeface
import android.support.v4.app.FragmentManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_song.view.*
import me.kosert.youtubeplayer.App
import me.kosert.youtubeplayer.GlobalProvider
import me.kosert.youtubeplayer.R
import me.kosert.youtubeplayer.music.MusicQueue
import me.kosert.youtubeplayer.service.PlayingState
import me.kosert.youtubeplayer.service.Song
import me.kosert.youtubeplayer.ui.dialogs.PlaylistsDialog
import java.text.DecimalFormat

class SongAdapter(
        private val playerView: PlayerView,
        private val fragmentManager: FragmentManager,
        private val items: List<Song>
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        val holder = SongViewHolder(inflatedView)
        holder.container.setOnClickListener(holder.onClick)
        holder.moreButton.setOnClickListener(holder.onMoreClick)
        return holder
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class SongViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        val container = v.container
        val numberTextView = v.numberText
        val titleTextView = v.titleText
        val lengthTextView = v.lengthText
        val moreButton = v.moreButton

        fun bind(position: Int) {

            val song = items[position]

            if (song.ytUrl == GlobalProvider.currentState.song?.ytUrl) {
                titleTextView.setTypeface(null, Typeface.BOLD)

                when(GlobalProvider.currentState.state) {
                    PlayingState.PLAYING ->
                        moreButton.setImageDrawable(App.get().getDrawable(R.drawable.ic_play_circle_outline_black_24dp))
                    PlayingState.PAUSED ->
                        moreButton.setImageDrawable(App.get().getDrawable(R.drawable.ic_pause_circle_outline_black_24dp))
                    PlayingState.STOPPED ->
                        moreButton.setImageDrawable(App.get().getDrawable(R.drawable.ic_more_vert_black_24dp))
                }

            } else {
                titleTextView.setTypeface(null, Typeface.NORMAL)
                moreButton.setImageDrawable(App.get().getDrawable(R.drawable.ic_more_vert_black_24dp))
            }

            numberTextView.text = (position + 1).toString()
            titleTextView.text = song.title
            val formatter = DecimalFormat("00")
            val lengthString = "${song.length / 60}:${formatter.format(song.length % 60)}"
            lengthTextView.text = lengthString
        }

        val onClick = View.OnClickListener {
            playerView.onSongSelected(adapterPosition)
        }

        val onMoreClick = View.OnClickListener {

            val song = items[adapterPosition]
            if (song.ytUrl == GlobalProvider.currentState.song?.ytUrl && GlobalProvider.currentState.state != PlayingState.STOPPED)
                return@OnClickListener

            val context = playerView as Context
            PopupMenu(context, moreButton).apply {
                inflate(R.menu.song_more)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.changePlaylist -> moveToPlaylist()
                        R.id.remove -> remove()
                    }
                    true
                }
            }.show()
        }

        private fun moveToPlaylist() {

            PlaylistsDialog.newInstance("Change playlist").apply {
                onSelectedAction = {
                    MusicQueue.moveToPlaylist(adapterPosition, it)
                }
            }.show(fragmentManager, PlaylistsDialog.TAG)

        }

        private fun remove() {
            val song = items[adapterPosition]

            if (GlobalProvider.currentState.state == PlayingState.STOPPED || song.ytUrl != GlobalProvider.currentState.song?.ytUrl) {
                playerView.onSongRemoveClicked(adapterPosition)
            }
        }
    }
}