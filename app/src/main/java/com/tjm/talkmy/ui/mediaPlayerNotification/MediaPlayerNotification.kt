package com.tjm.talkmy.ui.mediaPlayerNotification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.orhanobut.logger.Logger
import com.tjm.talkmy.MainActivity
import com.tjm.talkmy.R
import com.tjm.talkmy.ui.taskEdit.EditTaskFragment

class MediaPlayerNotification() : Service() {
    private val channelId = "media_player_channel"
    private val notificationId = 1
    private lateinit var mediaSession: MediaSessionCompat

    fun togglePlayPause(play:Boolean = true) = sendBroadcast(Intent(if(play) EditTaskFragment.IntentActions.PLAY else EditTaskFragment.IntentActions.PAUSE))
    fun changeNextPrev(next:Boolean) = sendBroadcast(Intent(if(next) EditTaskFragment.IntentActions.NEXT else EditTaskFragment.IntentActions.PREV))
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { handleIntent(it) }
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            "START" -> showNotification()
            "PLAY" -> updatePlaybackState(true)
            "PAUSE" -> updatePlaybackState(false)
            "STOP" -> stopSelf()
        }
    }

    private fun showNotification() {
        createNotificationChannel()
        initializeMediaSession()
        updatePlaybackState(false)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE )

        val notification = buildNotification(pendingIntent)
        startForeground(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.getNotificationChannel(channelId) ?: run {
                val channelName = "Media Player"
                val importance = NotificationManager.IMPORTANCE_LOW
                val channel = NotificationChannel(channelId, channelName, importance)
                notificationManager?.createNotificationChannel(channel)
            }
        }
    }

    private fun initializeMediaSession() {
        mediaSession = MediaSessionCompat(this, "media_session_tag8787").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    super.onPlay()
                    updatePlaybackState(true)
                }

                override fun onPause() {
                    super.onPause()
                    updatePlaybackState(false)
                }

                override fun onSkipToNext() {
                    super.onSkipToNext()
                    changeNextPrev(true)
                }

                override fun onSkipToPrevious() {
                    super.onSkipToPrevious()
                    changeNextPrev(false)
                }
            })
            isActive = true
        }
    }

    private fun updatePlaybackState(play: Boolean) {
        val state =
            if (play) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        val builder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PAUSE
                        or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
            .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
        val metaData = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Talkmy!")
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Artista")
            .build()
        mediaSession.setPlaybackState(builder.build())
        mediaSession.setMetadata(metaData)
        togglePlayPause(play)
    }

    private fun buildNotification(pendingIntent: PendingIntent): Notification {
        val pauseAction = NotificationCompat.Action.Builder(
            R.drawable.ic_play_notify, "Pause", PendingIntent.getBroadcast(this, 0, Intent("PAUSE"), PendingIntent.FLAG_IMMUTABLE)
        ).build()
        val nextAction = NotificationCompat.Action.Builder(
            R.drawable.ic_next, "Next", PendingIntent.getBroadcast(this, 0, Intent("NEXT"), PendingIntent.FLAG_IMMUTABLE)
        ).build()
        val prevAction = NotificationCompat.Action.Builder(
            R.drawable.ic_rewind, "Prev", PendingIntent.getBroadcast(this, 0, Intent("PREV"), PendingIntent.FLAG_IMMUTABLE)
        ).build()
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_listen)
            .setContentTitle("Talkmy")
            .setContentText("Artista")
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAllowSystemGeneratedContextualActions(true)
            .setOngoing(true)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2, 3)
            )
            .addAction(prevAction)
            .addAction(pauseAction)
            .addAction(nextAction)
            .build()
    }
    override fun onDestroy() {
        super.onDestroy()
        try {
            stopSelf()
            mediaSession.isActive = false
            mediaSession.release()
        } catch (e: Exception) {
            Logger.e(e.toString())
        }
    }
}
