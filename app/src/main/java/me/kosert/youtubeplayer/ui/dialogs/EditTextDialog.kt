package me.kosert.youtubeplayer.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.widget.EditText
import android.widget.LinearLayout

class EditTextDialog : DialogFragment() {

    val title by lazy { arguments?.getString(EXTRA_TITLE) }
    val initial by lazy { arguments?.getString(EXTRA_INITIAL_VALUE) }

    var onStringChosenAction: ((String) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return AlertDialog.Builder(context).apply {
            setTitle(title)

            val input = EditText(context)
            val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            input.layoutParams = lp
            initial?.let { input.setText(initial) }
            setView(input)
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton("OK") { dialog, _ ->
                onStringChosenAction?.invoke(input.text.toString())
            }
        }.create()
    }

    fun show(fragmentManager: FragmentManager) = show(fragmentManager, TAG)

    companion object {
        private const val EXTRA_TITLE = "EXTRA_TITLE"
        private const val EXTRA_INITIAL_VALUE = "INITIAL_VALUE"
        const val TAG = "me.kosert.youtubeplayer.ui.dialogs.EditTextDialog"

        fun newInstance(title: String = "Choose playlist", initialValue: String = ""): EditTextDialog {
            return EditTextDialog().apply {
                arguments = Bundle().apply { putString(EXTRA_TITLE, title) }
                arguments = Bundle().apply { putString(EXTRA_INITIAL_VALUE, initialValue) }
            }
        }
    }

}
