package com.tjm.talkmy.domain.interfaces

import android.widget.EditText
import com.tjm.talkmy.domain.models.AllPreferences
import com.tjm.talkmy.domain.models.Sentence

interface TTSManagerInterface {
    //fun togglePlayback(sentences: List<String>?, editText: EditText)

    fun togglePlayback(dirtySentences: String)


    fun speak()

    fun pause()

    fun destroyTTS()
    fun findStartByAproxStart(start: Int, dirtySentences: String)
    fun configTTS(preferences: AllPreferences)
}