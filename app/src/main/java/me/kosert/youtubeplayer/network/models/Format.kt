package me.kosert.youtubeplayer.network.models

import com.google.gson.annotations.Expose

class Format(
        @Expose val itag: Int,
        @Expose val url: String,
        @Expose val quality: String?,
        @Expose val type: String,
        @Expose val resolution: String?,
        @Expose val encoding: String?,
        @Expose val container: String,
        @Expose val audioEncoding: String?,
        @Expose val audioBitrate: String?

) {

    override fun toString(): String {
        return "Format(itag=$itag, url='$url', quality='$quality', type='$type', resolution='$resolution', encoding='$encoding', container='$container', audioEncoding='$audioEncoding', audioBitrate='$audioBitrate')"
    }
}