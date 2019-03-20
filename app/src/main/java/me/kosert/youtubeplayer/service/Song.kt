package me.kosert.youtubeplayer.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.annotations.Expose
import me.kosert.youtubeplayer.App
import me.kosert.youtubeplayer.network.models.Format
import java.io.File
import java.io.FileNotFoundException

class Song(
        @Expose val title: String,
        @Expose val ytUrl: String,
        @Expose val length: Int,    // in seconds
        /*@Expose*/ val format: Format?
) {

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

    override fun toString(): String {
        return "Song(title='$title', ytUrl='$ytUrl', length=$length)"
    }
}