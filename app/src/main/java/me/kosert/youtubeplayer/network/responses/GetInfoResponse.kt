package me.kosert.youtubeplayer.network.responses

import com.google.gson.annotations.Expose
import me.kosert.youtubeplayer.network.models.Format

class GetInfoResponse(
        @Expose val title: String,
        @Expose val url: String,
        @Expose val formats: List<Format>
) : AbstractResponseMessage() {

    override fun toString(): String {
        return "GetInfoResponse(title='$title', url='$url', formats=$formats)"
    }
}