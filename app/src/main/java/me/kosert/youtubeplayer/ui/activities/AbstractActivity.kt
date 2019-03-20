package me.kosert.youtubeplayer.ui.activities

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.support.annotation.CallSuper
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import me.kosert.channelbus.EventReceiver
import me.kosert.youtubeplayer.ui.dialogs.ProgressDialog
import me.kosert.youtubeplayer.util.Logger

abstract class AbstractActivity : AppCompatActivity(), IAbstractActivity {

    protected val logger = Logger(this.javaClass.simpleName)
    protected val handler = Handler()
    protected val receiver by lazy { EventReceiver() }

    private val flowLogger = Logger("FLOW")

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        flowLogger.i("onStart - ${this::class.java.simpleName}")
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        flowLogger.i("onStop - ${this::class.java.simpleName}")
        receiver.unsubscribeAll()
    }

    override fun showToast(text: String, length: Int) {
        Toast.makeText(this, text, length).show()
    }

    override fun showToast(resId: Int, length: Int) {
        Toast.makeText(this, resId, length).show()
    }

    override fun showSnack(text: String, length: Int) {
        val snackbar = Snackbar.make(window.decorView.findViewById(android.R.id.content), text, length)
        val textView = snackbar.view.findViewById<TextView>(android.support.design.R.id.snackbar_text)
        textView.maxLines = 1
        snackbar.show()
    }

    override fun showSnack(resId: Int, length: Int) {
        showSnack(getString(resId))
    }

    override fun showProgress(visible: Boolean) {
        if (visible) {
            val progressDialog = supportFragmentManager.findFragmentByTag(PROGRESS_TAG) as ProgressDialog?
                    ?: ProgressDialog.newInstance()
            progressDialog.show(supportFragmentManager, PROGRESS_TAG)
        }
        else {
            supportFragmentManager.findFragmentByTag(PROGRESS_TAG)?.let {
                it as ProgressDialog
                it.dismiss()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    companion object {
        private const val PROGRESS_TAG = "progressDialog"
    }
}