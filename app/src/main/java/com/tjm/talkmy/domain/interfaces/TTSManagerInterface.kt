package com.tjm.talkmy.domain.interfaces

import com.tjm.talkmy.domain.models.AllPreferences

interface TTSManagerInterface {
    //fun togglePlayback(sentences: List<String>?, editText: EditText)

    fun togglePlayback(listOfSentences: List<String>? = null, indice: Int? = null, play:Boolean? = null)


    fun speak()

    fun pause()

    fun destroyTTS()
    fun findStartByAproxStart(start: Int, dirtySentences: String)
    fun configTTS(preferences: AllPreferences)
}