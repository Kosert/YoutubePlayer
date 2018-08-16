package me.kosert.youtubeplayer.ui.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.kosert.youtubeplayer.R

class ProgressDialog : DialogFragment()
{
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
	{
		isCancelable = false
		return inflater.inflate(R.layout.progress_dialog, container, true)
	}

	companion object {
		fun newInstance() : ProgressDialog {
			return ProgressDialog()
		}
	}
}