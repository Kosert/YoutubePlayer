package me.kosert.youtubeplayer.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.squareup.otto.Subscribe
import me.kosert.youtubeplayer.GlobalProvider
import me.kosert.youtubeplayer.R
import me.kosert.youtubeplayer.music.MusicProvider
import me.kosert.youtubeplayer.music.MusicQueue
import me.kosert.youtubeplayer.music.PlayingStateEvent
import me.kosert.youtubeplayer.music.State
import me.kosert.youtubeplayer.ui.activities.player.PlayerActivity
import me.kosert.youtubeplayer.util.Logger


class PlayerService : Service() {

    private val bus = GlobalProvider.bus
    private val logger = Logger("PlayerService")
    private val handler = Handler()

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        logger.i("Creating")
        bus.register(this)

        startForeground(ONGOING_NOTIFICATION_ID, createNotification(MusicQueue.currentState))
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.i("Destroying")
        bus.unregister(this)

        mediaPlayer?.apply {
            release()
        }
    }


    @Subscribe
    fun onControlEvent(event: ControlEvent) {
        when (event.type) {
            OperationType.PLAY -> play()
            OperationType.PAUSE -> pause()
            else -> TODO()
        }
    }

    @Subscribe
    fun onPlayingStateChange(event: PlayingStateEvent) {
        updateNotification()
    }

    private fun play() {

        mediaPlayer?.let {
            if (MusicQueue.currentState == State.PAUSED) {
                it.start()
                return@play
            }
        }

        val song = MusicQueue.getFirst() ?: return
        if (MusicProvider.isSongSaved(song)) {
            //TODO if song is saved
        } else {
            val uri = MusicProvider.getSongUri(song)

            val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
                setDataSource(uri)
                setOnCompletionListener(onPlaybackCompleted)
                prepare()
                start()
            }
        }

    }

    private fun pause() {
        mediaPlayer?.let {
            MusicQueue.onPaused()
            it.pause()
        }
    }

    private val onPlaybackCompleted = MediaPlayer.OnCompletionListener {
        MusicQueue.onFinishedPlaying()
        play()
    }

    private fun updateNotification() {
        val notification = createNotification(MusicQueue.currentState)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ONGOING_NOTIFICATION_ID, notification)
    }

    private fun createNotification(state: State): Notification {
        val notificationIntent = Intent(this, PlayerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW)
            channel.description = "Player Service"
            notificationManager.createNotificationChannel(channel)
        }

        val exitAction = run {
            val exitIntent = Intent(GlobalProvider.SHUTDOWN_ACTION)
            val pendingExitIntent = PendingIntent.getBroadcast(this, 0, exitIntent, 0)
            NotificationCompat.Action.Builder(R.drawable.ic_close_black_24dp, "Exit", pendingExitIntent).build()
        }
        val stopAction = run {
            val stopIntent = Intent(GlobalProvider.STOP_ACTION)
            val pendingStopIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0)
            NotificationCompat.Action.Builder(R.drawable.ic_stop_black_24dp, "Stop", pendingStopIntent).build()
        }

        val playPauseAction = if (state == State.PLAYING) {
            val pauseIntent = Intent(GlobalProvider.PAUSE_ACTION)
            val pendingPauseIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, 0)
            NotificationCompat.Action.Builder(R.drawable.ic_pause_black_24dp, "Pause", pendingPauseIntent).build()
        } else {
            val playIntent = Intent(GlobalProvider.PLAY_ACTION)
            val pendingPlayIntent = PendingIntent.getBroadcast(this, 0, playIntent, 0)
            NotificationCompat.Action.Builder(R.drawable.ic_play_arrow_black_24dp, "Play", pendingPlayIntent).build()
        }

        //TODO skip forward action

        val drawable = getDrawable(R.mipmap.ic_launcher) as BitmapDrawable

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Player is running")
                .setSmallIcon(R.drawable.ic_youtubes)
                .setLargeIcon(drawable.bitmap)
                .setContentIntent(pendingIntent)
                .addAction(playPauseAction)
                .addAction(stopAction)
                .addAction(exitAction)
                .build()
    }

    companion object {
        const val ONGOING_NOTIFICATION_ID = 2137
        const val NOTIFICATION_CHANNEL_ID = "PlayerServiceID"
        const val NOTIFICATION_CHANNEL_NAME = "Player Service"
    }
}
