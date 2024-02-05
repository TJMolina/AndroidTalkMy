package com.tjm.talkmy.ui.taskEdit.managers

import android.content.Context
import android.media.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MyAudioManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    fun increaseVolume(volume:Int) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,volume, 0)
    }
    fun actualVolume():Int = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
}