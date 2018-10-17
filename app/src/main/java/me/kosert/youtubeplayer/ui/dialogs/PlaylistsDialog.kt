package me.kosert.youtubeplayer.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.playlists_dialog.view.*
import me.kosert.youtubeplayer.R
import me.kosert.youtubeplayer.memory.AppData
import me.kosert.youtubeplayer.memory.AppData.StringType.*
import me.kosert.youtubeplayer.music.MusicQueue

class PlaylistsDialog : DialogFragment() {

    val title by lazy { arguments?.getString(EXTRA_TITLE) }

    var onSelectedAction : ((PlaylistItem) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = LayoutInflater.from(context).inflate(R.layout.playlists_dialog, null)

        view.playlistsRecycler.layoutManager = LinearLayoutManager(context)
        view.playlistsRecycler.adapter = PlaylistAdapter(context!!, getItems()) {
            onSelectedAction?.invoke(it)
            this.dismiss()
        }

        return AlertDialog.Builder(context).apply {
            setTitle(title)
            setView(view)
        }.create()
    }

    private fun getItems(): List<PlaylistItem> {
        return listOf(SAVED_NAME_1, SAVED_NAME_2, SAVED_NAME_3)
                .mapIndexed { i, name ->
                    PlaylistItem(i + 1, AppData.getString(name))
                }
    }

    companion object {
        private const val EXTRA_TITLE = "EXTRA_TITLE"
        const val TAG = "me.kosert.youtubeplayer.ui.dialogs.PlaylistsDialog"
        fun newInstance(title: String = "Choose playlist"): PlaylistsDialog {
            return PlaylistsDialog().apply {
                arguments = Bundle().apply { putString(EXTRA_TITLE, title) }
            }
        }
    }

}
