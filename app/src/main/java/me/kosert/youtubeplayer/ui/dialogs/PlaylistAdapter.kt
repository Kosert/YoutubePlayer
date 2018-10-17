package me.kosert.youtubeplayer.ui.dialogs

import android.content.Context
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.playlist_item.view.*
import me.kosert.youtubeplayer.R
import android.widget.EditText
import me.kosert.youtubeplayer.memory.AppData


class PlaylistAdapter(
        private val context: Context,
        private val items: List<PlaylistItem>,
        private val clickListener: (PlaylistItem) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.playlist_item, parent, false)
        return PlaylistViewHolder(inflatedView)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class PlaylistViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        val nameTextView: TextView = v.nameTextView
        val editButton: ImageView = v.editButton

        fun bind(position: Int) {
            val item = items[position]
            nameTextView.text = item.name

            nameTextView.setOnClickListener {
                clickListener(item)
            }

            editButton.setOnClickListener {
                AlertDialog.Builder(context).apply {
                    setTitle("Change playlist name")

                    val input = EditText(context)
                    val lp = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
                    input.layoutParams = lp
                    setView(input)
                    setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    setPositiveButton("OK") { dialog, _ ->
                        val prefType = when (item.number) {
                            1 -> AppData.StringType.SAVED_NAME_1
                            2 -> AppData.StringType.SAVED_NAME_2
                            3 -> AppData.StringType.SAVED_NAME_3
                            else -> throw IllegalArgumentException()
                        }
                        AppData.setString(prefType, input.text.toString())
                        dialog.dismiss()
                    }
                }.show()
            }
        }
    }
}