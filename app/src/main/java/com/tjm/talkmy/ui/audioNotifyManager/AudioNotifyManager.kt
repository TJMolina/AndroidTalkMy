package com.tjm.talkmy.ui.audioNotifyManager

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tjm.talkmy.R


class AudioNotifyManager: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }
    private fun start(){
        val notification = NotificationCompat.Builder(this, "running_channel")
            .setSmallIcon(R.drawable.ic_talkmy)
            .setContentTitle("!Talkmy")
            .setContentText("leyendo")
            .build()
        startForeground(1, notification)
    }
    enum class Actions{
        START, STOP
    }
}