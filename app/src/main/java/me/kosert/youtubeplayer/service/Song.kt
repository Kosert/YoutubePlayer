package me.kosert.youtubeplayer.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.annotations.Expose
import com.squareup.otto.Subscribe
import me.kosert.youtubeplayer.App
import me.kosert.youtubeplayer.GlobalProvider
import me.kosert.youtubeplayer.network.Network
import me.kosert.youtubeplayer.network.NetworkResponseEvent
import me.kosert.youtubeplayer.network.models.Format
import me.kosert.youtubeplayer.network.requests.GetInfoRequest
import me.kosert.youtubeplayer.network.responses.GetInfoResponse
import java.io.File
import java.io.FileNotFoundException

class Song(
        @Expose val title: String,
        @Expose val ytUrl: String,
        @Expose val length: Int,    // in seconds
        /*@Expose*/ val format: Format?
) {

    private var listener: SongLoadedListener? = null

    private fun getVideoId() = ytUrl.split("=")[1].replaceAfter("&", "")
    private fun getImageURL() = "https://i.ytimg.com/vi/${getVideoId()}/hqdefault.jpg"

    fun getImage(): Bitmap? {
        return try {
            val inputStream = App.get().openFileInput(getVideoId())
            BitmapFactory.decodeStream(inputStream)
        }
        catch (e: FileNotFoundException) { null }
    }

    fun getMusicFile(): File {
        val name = "${getVideoId()}.m4a"//${format?.container}"
        val dir = App.get().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        return File(dir, name)
    }

    fun downloadImage() {
        Glide.with(App.get())
                .asBitmap()
                .load(getImageURL())
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val outStream = App.get().openFileOutput(getVideoId(), Context.MODE_PRIVATE)
                        resource.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                        outStream.close()
                    }
                })
    }

    fun downloadFormats(listener: SongLoadedListener) {
        this.listener = listener
        GlobalProvider.bus.register(this)
        val request = GetInfoRequest(ytUrl)
        Network.send(request)
    }

    @Subscribe
    fun onNewNetworkEvent(event: NetworkResponseEvent) {

        if (event.responseMessage !is GetInfoResponse || event.responseMessage.url != ytUrl)
            return

        GlobalProvider.bus.unregister(this)
        val response = event.responseMessage
        val audioFormats = response.formats.filter {
            it.audioEncoding != null && it.audioEncoding != "opus" && it.encoding == null
        }.sortedByDescending { it.audioBitrate?.toInt() }

        if (audioFormats.isNotEmpty()) {
            val format = audioFormats[0]
            //val song = Song(response.title, response.url, response.length, format)
            listener?.onSongLoaded(format.url)
        } else {
            //listener?.onError()
        }
    }

    override fun toString(): String {
        return "Song(title='$title', ytUrl='$ytUrl', length=$length)"
    }
}