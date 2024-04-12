package com.tjm.talkmy.domain.interfaces

import com.tjm.talkmy.domain.models.AllPreferences

interface TTSManagerInterface {
    //fun togglePlayback(sentences: List<String>?, editText: EditText)

    fun togglePlayback(listOfSentences: List<String>, indice: Int)


    fun speak()

    fun pause()

    fun destroyTTS()
    fun findStartByAproxStart(start: Int, dirtySentences: String)
    fun configTTS(preferences: AllPreferences)
}