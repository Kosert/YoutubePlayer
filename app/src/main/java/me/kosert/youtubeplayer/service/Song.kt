package me.kosert.youtubeplayer.service

import com.google.gson.annotations.Expose
import me.kosert.youtubeplayer.network.models.Format

class Song(
        @Expose val title: String,
        @Expose val ytUrl: String,
        @Expose val length: Int,    // in seconds
        @Expose val format: Format
)