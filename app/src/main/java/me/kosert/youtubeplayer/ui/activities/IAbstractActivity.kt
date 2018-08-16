package me.kosert.youtubeplayer.ui.activities

import android.support.design.widget.Snackbar
import android.widget.Toast

interface IAbstractActivity
{
	fun showToast(text: String, length: Int = Toast.LENGTH_SHORT)
	fun showToast(resId: Int, length: Int = Toast.LENGTH_SHORT)

	fun showSnack(text: String, length: Int = Snackbar.LENGTH_SHORT)
	fun showSnack(resId: Int, length: Int = Snackbar.LENGTH_SHORT)

	fun showProgress(visible: Boolean)
}