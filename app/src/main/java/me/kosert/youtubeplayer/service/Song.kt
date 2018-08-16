package me.kosert.youtubeplayer.service

import me.kosert.youtubeplayer.network.models.Format

class Song(
        val title: String,
        val ytUrl: String,
        val format: Format
)