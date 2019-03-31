package me.kosert.youtubeplayer.network.responses

import com.google.gson.annotations.Expose
import me.kosert.youtubeplayer.network.models.Format

class GetInfoResponse(
        @Expose val title: String,
        @Expose val url: String,
        @Expose val length: Int,
        @Expose val formats: List<Format>?,
        @Expose val thumb: String
) : AbstractResponseMessage() {

    override fun toString(): String {
        return "GetInfoResponse(title='$title', url='$url', length='$length', formats=$formats, thumb=$thumb)"
    }
}