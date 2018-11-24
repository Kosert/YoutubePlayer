package me.kosert.youtubeplayer.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.BitmapDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.media.app.NotificationCompat.MediaStyle
import android.view.KeyEvent
import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import me.kosert.youtubeplayer.GlobalProvider
import me.kosert.youtubeplayer.R
import me.kosert.youtubeplayer.music.MusicProvider
import me.kosert.youtubeplayer.music.QueueChangedEvent
import me.kosert.youtubeplayer.music.StateEvent
import me.kosert.youtubeplayer.receivers.AppShutdownReceiver
import me.kosert.youtubeplayer.receivers.ControlReceiver
import me.kosert.youtubeplayer.receivers.HeadsetConnectionReceiver
import me.kosert.youtubeplayer.ui.activities.player.PlayerActivity
import me.kosert.youtubeplayer.util.Logger
import java.io.FileInputStream


class PlayerService : Service() {

    private val bus = GlobalProvider.bus
    private val logger = Logger("PlayerService")
    private val timeHandler = Handler()
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSession
    private lateinit var headsetReceiver: HeadsetConnectionReceiver

    private val controller = NowPlayingController()

    @Produce
    fun getLastStateEvent(): StateEvent? {
        return GlobalProvider.currentState
    }

    private fun getCurrentPlayingState() = GlobalProvider.currentState.state

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        logger.i("Creating")
        bus.register(this)

        mediaSession = MediaSession(this, "PlayerService")
        mediaSession.setCallback(object : MediaSession.Callback() {
            override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {

                val keyEvent = mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                if (keyEvent.action != KeyEvent.ACTION_DOWN) return true

                logger.i("Media button pressed")
                if (GlobalProvider.currentState.state == PlayingState.PLAYING)
                    bus.post(ControlEvent(OperationType.PAUSE))
                else
                    bus.post(ControlEvent(OperationType.PLAY))

                return true
            }
        })
        mediaSession.isActive = true
        headsetReceiver = HeadsetConnectionReceiver()
        registerReceiver(headsetReceiver, IntentFilter(AudioManager.ACTION_HEADSET_PLUG))
        startForeground(ONGOING_NOTIFICATION_ID, createNotification(getCurrentPlayingState()))
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger.i("onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.i("Destroying")
        bus.unregister(this)
        //MusicQueue.uninit()
        mediaSession.isActive = false
        unregisterReceiver(headsetReceiver)
        timeHandler.removeCallbacksAndMessages(null)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()

        mediaPlayer?.apply {
            release()
        }
    }

    //TODO do wyjebania?
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
            OperationType.SELECTED -> selectSong(event.index)
            OperationType.NEXT -> goNext() //MusicQueue.onNext()
            OperationType.PLAYLIST_SWAP -> onSwap()
        }

        if (event.type == OperationType.PLAY) {
            postPlayingTime()
        }
        updateNotification()
    }

    private fun postPlayingTime() {
        mediaPlayer?.let {
            GlobalProvider.currentState = StateEvent(PlayingState.PLAYING, controller.currentSong, it.currentPosition)
            updateNotification()
        }

        timeHandler.postDelayed({
            postPlayingTime()
        }, 500)
    }

    private fun play() {

        mediaPlayer?.let {
            if (getCurrentPlayingState() == PlayingState.PAUSED) {
                it.start()
                GlobalProvider.currentState = StateEvent(PlayingState.PLAYING, controller.currentSong, it.currentPosition)
                return
            }
        }

        val song = controller.currentSong ?: return
        if (MusicProvider.isSongSaved(song)) {
            loadSong(song)
        } else {
            //TODO wyjebac to, download na dodaniu, tutaj jak nie ma to goNext
//            MusicProvider.fetchSong(song, object : SongLoadedListener {
//                override fun onSongLoaded(uri: String) {
//                    loadSong(song)
//                }
//            })
        }

    }

    private fun loadSong(song: Song) {
        val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

        stop()
        val stream = FileInputStream(song.getMusicFile())
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(audioAttributes)
            setDataSource(stream.fd)
            setOnCompletionListener(onPlaybackCompleted)
            setOnPreparedListener {
                GlobalProvider.currentState = StateEvent(PlayingState.PLAYING, controller.currentSong, it.currentPosition)
                start()
            }
            prepareAsync()
        }
    }

    private fun pause() {
        mediaPlayer?.let {
            it.pause()
            GlobalProvider.currentState = StateEvent(PlayingState.PAUSED, controller.currentSong, it.currentPosition)
        }
    }

    private fun stop() {
        mediaPlayer?.apply {
            stop()
            release()
            GlobalProvider.currentState = StateEvent(PlayingState.STOPPED, controller.currentSong, 0)
        }
        mediaPlayer = null
    }

    private fun onSwap() {
        stop()
        controller.selectSong(-1)
        GlobalProvider.currentState = StateEvent(PlayingState.STOPPED, controller.currentSong, 0)
    }

    private val onPlaybackCompleted = MediaPlayer.OnCompletionListener {
        logger.i("Playback completed")
        GlobalProvider.currentState = StateEvent(PlayingState.STOPPED, controller.currentSong, 0)
        if (controller.getNext() != null) {
            controller.goNext()
            play()
        }
    }

    private fun goNext() {
        bus.post(ControlEvent(OperationType.STOP))
        controller.goNext()
        bus.post(ControlEvent(OperationType.PLAY))
    }

    private fun selectSong(position: Int) {
        bus.post(ControlEvent(OperationType.STOP))
        controller.selectSong(position)
        bus.post(ControlEvent(OperationType.PLAY))
    }

    private fun updateNotification() {
        logger.d(getCurrentPlayingState().toString())
        val notification = createNotification(getCurrentPlayingState())

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ONGOING_NOTIFICATION_ID, notification)
    }

    private fun createNotification(state: PlayingState): Notification {
        val notificationIntent = Intent(this, PlayerActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(this)
                .addNextIntent(notificationIntent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Player Service"
            notificationManager.createNotificationChannel(channel)
        }

        val exitAction = run {
            val exitIntent = Intent(this, AppShutdownReceiver::class.java)
            exitIntent.action = GlobalProvider.SHUTDOWN_ACTION
            val pendingExitIntent = PendingIntent.getBroadcast(this, 0, exitIntent, 0)
            NotificationCompat.Action.Builder(R.drawable.ic_close_black_24dp, "EXIT", pendingExitIntent).build()
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
            NotificationCompat.Action.Builder(R.drawable.ic_skip_next_black_24dp, "NEXT", pendingNextIntent).build()
        }

        val playPauseAction = if (state == PlayingState.PLAYING) {
            val pauseIntent = Intent(this, ControlReceiver::class.java)
            pauseIntent.action = GlobalProvider.PAUSE_ACTION
            val pendingPauseIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, 0)
            NotificationCompat.Action.Builder(R.drawable.ic_pause_black_24dp, "PAUSE", pendingPauseIntent).build()
        } else {
            val playIntent = Intent(this, ControlReceiver::class.java)
            playIntent.action = GlobalProvider.PLAY_ACTION
            val pendingPlayIntent = PendingIntent.getBroadcast(this, 0, playIntent, 0)
            NotificationCompat.Action.Builder(R.drawable.ic_play_arrow_black_24dp, "PLAY", pendingPlayIntent).build()
        }

        val bitmap = controller.currentSong?.getImage() ?: run {
            controller.currentSong?.downloadImage()
            val drawable = getDrawable(R.mipmap.ic_launcher) as BitmapDrawable
            drawable.bitmap
        }

        val text = controller.currentSong?.title ?: run { "No songs in queue" }
        val subtitle = controller.getNext()?.title?.let { "Next: $it" }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).apply {
            setContentTitle(text)
            subtitle?.let { setContentText(it) }
            setStyle(MediaStyle().setShowActionsInCompactView(0, 1, 2))
            setSmallIcon(R.drawable.ic_youtubes)
            setLargeIcon(bitmap)
            setContentIntent(pendingIntent)
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
