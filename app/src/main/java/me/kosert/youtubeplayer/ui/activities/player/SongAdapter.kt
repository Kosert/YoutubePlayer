package me.kosert.youtubeplayer.ui.activities.player

import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_song.view.*
import me.kosert.youtubeplayer.App
import me.kosert.youtubeplayer.R
import me.kosert.youtubeplayer.music.MusicQueue
import me.kosert.youtubeplayer.music.State
import me.kosert.youtubeplayer.service.Song
import java.text.DecimalFormat

class SongAdapter(private val items: List<Song>) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        val holder = SongViewHolder(inflatedView)
        holder.container.setOnClickListener(holder.onClick)
        holder.removeButton.setOnClickListener(holder.onRemoveClick)
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
        val removeButton = v.removeButton

        fun bind(position: Int) {

            val song = items[position]

            if (song.ytUrl == MusicQueue.getCurrent()?.ytUrl) {
                titleTextView.setTypeface(null, Typeface.BOLD)

                when(MusicQueue.currentState) {
                    State.PLAYING ->
                        removeButton.setImageDrawable(App.get().getDrawable(R.drawable.ic_play_circle_outline_black_24dp))
                    State.PAUSED ->
                        removeButton.setImageDrawable(App.get().getDrawable(R.drawable.ic_pause_circle_outline_black_24dp))
                    State.STOPPED ->
                        removeButton.setImageDrawable(App.get().getDrawable(R.drawable.ic_close_black_24dp))
                }

            } else {
                titleTextView.setTypeface(null, Typeface.NORMAL)
                removeButton.setImageDrawable(App.get().getDrawable(R.drawable.ic_close_black_24dp))
            }

            numberTextView.text = (position + 1).toString()
            titleTextView.text = song.title
            val formatter = DecimalFormat("00")
            val lengthString = "${song.length / 60}:${formatter.format(song.length % 60)}"
            lengthTextView.text = lengthString
        }

        val onClick = View.OnClickListener {
            MusicQueue.onSongSelected(adapterPosition)
        }

        val onRemoveClick = View.OnClickListener {
            val song = items[adapterPosition]
            if (MusicQueue.currentState == State.STOPPED || song.ytUrl != MusicQueue.getCurrent()?.ytUrl)
                MusicQueue.removeSong(adapterPosition)
        }
    }
}