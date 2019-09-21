package me.kosert.youtubeplayer.service

import android.media.session.PlaybackState
import android.support.v4.media.session.PlaybackStateCompat

enum class PlayingState(
        @PlaybackStateCompat.State val stateInt: Int
) {

    PLAYING(PlaybackState.STATE_PLAYING),
    PAUSED(PlaybackState.STATE_PAUSED),
    STOPPED(PlaybackState.STATE_STOPPED)
}