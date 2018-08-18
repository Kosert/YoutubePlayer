package me.kosert.youtubeplayer.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.squareup.otto.Subscribe
import me.kosert.youtubeplayer.GlobalProvider
import me.kosert.youtubeplayer.R
import me.kosert.youtubeplayer.music.*
import me.kosert.youtubeplayer.receivers.AppShutdownReceiver
import me.kosert.youtubeplayer.receivers.ControlReceiver
import me.kosert.youtubeplayer.ui.activities.player.PlayerActivity
import me.kosert.youtubeplayer.util.Logger


class PlayerService : Service() {

    private val bus = GlobalProvider.bus
    private val logger = Logger("PlayerService")
    private val timeHandler = Handler()
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSession

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        logger.i("Creating")
        bus.register(this)

        mediaSession = MediaSession(this, "PlayerService")
        mediaSession.setCallback(object : MediaSession.Callback() {
            override fun onMediaButtonEvent(mediaButtonIntent: Intent?): Boolean {
                logger.i("Media button pressed ($mediaButtonIntent)")

                if (MusicQueue.currentState == State.PLAYING)
                    bus.post(ControlEvent(OperationType.PAUSE))
                else
                    bus.post(ControlEvent(OperationType.PLAY))

                return super.onMediaButtonEvent(mediaButtonIntent)
            }
        })
        mediaSession.isActive = true
        startForeground(ONGOING_NOTIFICATION_ID, createNotification(MusicQueue.currentState))
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.i("Destroying")
        bus.unregister(this)
        MusicQueue.uninit()
        mediaSession.isActive = false
        timeHandler.removeCallbacksAndMessages(null)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()

        mediaPlayer?.apply {
            release()
        }
    }

    @Subscribe
    fun onPlayingStateChange(event: PlayingStateEvent) {
        updateNotification()
    }

    @Subscribe
    fun onQueueChanged(event: QueueChangedEvent) {
        updateNotification()
    }

    @Subscribe
    fun onControlEvent(event: ControlEvent) {
        logger.i("Control Event: " + event.type)
        timeHandler.removeCallbacksAndMessages(null)
        when (event.type) {
            OperationType.PLAY -> play()
            OperationType.PAUSE -> pause()
            OperationType.STOP -> stop()
            OperationType.NEXT -> MusicQueue.onNext()
        }

        if (event.type == OperationType.PLAY) {
            postPlayingTime()
        }
    }

    private fun postPlayingTime() {
        mediaPlayer?.let {
            MusicQueue.currentTime = it.currentPosition
            updateNotification()
        }

        timeHandler.postDelayed({
            postPlayingTime()
        }, 500)
    }

    private fun play() {

        mediaPlayer?.let {
            if (MusicQueue.currentState == State.PAUSED) {
                it.start()
                MusicQueue.onStarted()
                return
            }
        }

        val song = MusicQueue.getCurrent() ?: return
        if (MusicProvider.isSongSaved(song)) {
            //TODO if song is saved
        } else {
            val uri = MusicProvider.getSongUri(song)

            val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

            stop()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
                setDataSource(uri)
                setOnCompletionListener(onPlaybackCompleted)
                setOnPreparedListener {
                    MusicQueue.onStarted()
                    start()
                }
                prepareAsync()
            }
        }

    }

    private fun pause() {
        mediaPlayer?.let {
            it.pause()
            MusicQueue.onPaused()
        }
    }

    private fun stop() {
        mediaPlayer?.apply {
            stop()
            release()
            MusicQueue.onStopped()
        }
        mediaPlayer = null
    }

    private val onPlaybackCompleted = MediaPlayer.OnCompletionListener {
        logger.i("Playback completed")
        MusicQueue.onFinishedPlaying()
        if (MusicQueue.canPlayNext())
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Player Service"
            notificationManager.createNotificationChannel(channel)
        }

        val exitAction = run {
            val exitIntent = Intent(this, AppShutdownReceiver::class.java)
            exitIntent.action = GlobalProvider.SHUTDOWN_ACTION
            val pendingExitIntent = PendingIntent.getBroadcast(this, 0, exitIntent, 0)
            NotificationCompat.Action.Builder(R.drawable.ic_close_black_24dp, "Exit", pendingExitIntent).build()
        }
//        val stopAction = run {
//            val stopIntent = Intent(this, ControlReceiver::class.java)
//            stopIntent.action = GlobalProvider.STOP_ACTION
//            val pendingStopIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0)
//            NotificationCompat.Action.Builder(R.drawable.ic_stop_black_24dp, "Stop", pendingStopIntent).build()
//        }
        val nextAction = run {
            val nextIntent = Intent(this, ControlReceiver::class.java)
            nextIntent.action = GlobalProvider.NEXT_ACTION
            val pendingNextIntent = PendingIntent.getBroadcast(this, 0, nextIntent, 0)
            NotificationCompat.Action.Builder(R.drawable.ic_skip_next_black_24dp, "Next", pendingNextIntent).build()
        }

        val playPauseAction = if (state == State.PLAYING) {
            val pauseIntent = Intent(this, ControlReceiver::class.java)
            pauseIntent.action = GlobalProvider.PAUSE_ACTION
            val pendingPauseIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, 0)
            NotificationCompat.Action.Builder(R.drawable.ic_pause_black_24dp, "Pause", pendingPauseIntent).build()
        } else {
            val playIntent = Intent(this, ControlReceiver::class.java)
            playIntent.action = GlobalProvider.PLAY_ACTION
            val pendingPlayIntent = PendingIntent.getBroadcast(this, 0, playIntent, 0)
            NotificationCompat.Action.Builder(R.drawable.ic_play_arrow_black_24dp, "Play", pendingPlayIntent).build()
        }

        //val drawable = getDrawable(R.mipmap.ic_launcher) as BitmapDrawable
        val text = MusicQueue.getCurrent()?.title ?: run { "No songs in queue" }
        val max = (MusicQueue.getCurrent()?.length ?: 0) * 1000

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).apply {
            setContentTitle(text)
            setSmallIcon(R.drawable.ic_youtubes)
            //setLargeIcon(drawable.bitmap)
            setContentIntent(pendingIntent)
            if (MusicQueue.currentState != State.STOPPED)
                setProgress(max, MusicQueue.currentTime, false)
            addAction(playPauseAction)
            addAction(nextAction)
            //.addAction(stopAction)
            addAction(exitAction)
        }.build()
    }

    companion object {
        const val ONGOING_NOTIFICATION_ID = 2137
        const val NOTIFICATION_CHANNEL_ID = "PlayerServiceID"
        const val NOTIFICATION_CHANNEL_NAME = "Player Service"
    }
}
