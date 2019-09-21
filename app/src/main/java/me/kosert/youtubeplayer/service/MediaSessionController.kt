package me.kosert.youtubeplayer.service

import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.view.KeyEvent
import me.kosert.channelbus.EventsReceiver
import me.kosert.channelbus.GlobalBus
import me.kosert.youtubeplayer.GlobalProvider
import me.kosert.youtubeplayer.music.StateEvent
import me.kosert.youtubeplayer.util.Logger

class MediaSessionController(
        val context: Context
) {

    private val logger = Logger("MediaSessionController")
    private val receiver = EventsReceiver()

    private lateinit var mediaSession: MediaSession

    private val metaDataBuilder by lazy { MediaMetadata.Builder() }

    private val alwaysAvailableActions = PlaybackState.ACTION_PLAY_PAUSE or PlaybackState.ACTION_SKIP_TO_NEXT
    private val stateBuilder by lazy {
        PlaybackState.Builder().also {
            it.setActions(alwaysAvailableActions)
        }
    }

    private val mediaSessionCallbacks = object : MediaSession.Callback() {

        override fun onSkipToNext() {
            GlobalBus.post(ControlEvent(OperationType.NEXT))
        }

        override fun onStop() {
            GlobalBus.post(ControlEvent(OperationType.STOP))
        }

        override fun onPause() {
            GlobalBus.post(ControlEvent(OperationType.PAUSE))
        }

        override fun onPlay() {
            GlobalBus.post(ControlEvent(OperationType.PLAY))
        }

        override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
            val keyEvent = mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            if (keyEvent.action != KeyEvent.ACTION_DOWN) return true

            logger.i("Media button pressed")
            if (GlobalProvider.currentState.state == PlayingState.PLAYING)
                GlobalBus.post(ControlEvent(OperationType.PAUSE))
            else
                GlobalBus.post(ControlEvent(OperationType.PLAY))

            return true
        }
    }

    fun initialize() {
        mediaSession = MediaSession(context, "PlayerService")
        mediaSession.setCallback(mediaSessionCallbacks)
        mediaSession.isActive = true

        var lastState: StateEvent? = null
        receiver.subscribe { event: StateEvent ->
            if (lastState == null || lastState?.song?.ytUrl != event.song?.ytUrl)
                mediaSession.setMetadata(event.song?.getMetadata())

            if (lastState == null || lastState?.state != event.state)
                mediaSession.setPlaybackState(event.getPlaybackState())

            lastState = event
        }
    }

    fun uninitialize() {
        receiver.unsubscribeAll()
        mediaSession.isActive = false
    }

    private fun StateEvent.getPlaybackState(): PlaybackState {
        stateBuilder.setState(state.stateInt, millis.toLong(), 1f)

        val actions = when (state) {
            PlayingState.PLAYING -> PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_STOP
            PlayingState.PAUSED -> PlaybackState.ACTION_PLAY or PlaybackState.ACTION_STOP
            PlayingState.STOPPED -> PlaybackState.ACTION_PLAY
        }
        stateBuilder.setActions(alwaysAvailableActions or actions)
        return stateBuilder.build()
    }

    private fun Song.getMetadata(): MediaMetadata {

        val (artist, songName) = title.split("-").let { split ->

            if (split.size > 1)
                Pair(split.first(), split.drop(1).joinToString(""))
            else
                Pair("", title)
        }

        metaDataBuilder.putLong(MediaMetadata.METADATA_KEY_DURATION, length * 1000L)
        metaDataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
        metaDataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, songName)
        metaDataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, title)
        getImage()?.let {
            metaDataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, it)
            metaDataBuilder.putBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON, it)
        }
        return metaDataBuilder.build()
    }

}