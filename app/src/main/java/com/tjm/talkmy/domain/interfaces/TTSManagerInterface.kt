package com.tjm.talkmy.domain.interfaces

import android.widget.EditText
import com.tjm.talkmy.domain.models.AllPreferences
import com.tjm.talkmy.domain.models.Sentence

interface TTSManagerInterface {
    //fun togglePlayback(sentences: List<String>?, editText: EditText)

    fun togglePlayback(editText: EditText)


    fun speak(sentences: List<Sentence>, editText: EditText)

    fun pause()

    fun destroyTTS()
    fun findStartByAproxStart(start: Int, editText: EditText)
    fun configTTS(preferences: AllPreferences)
}