package com.tjm.talkmy.domain.interfaces

import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.widget.EditText
import com.tjm.talkmy.domain.models.AllPreferences

interface TTSManagerInterface {
    //fun togglePlayback(sentences: List<String>?, editText: EditText)

    fun togglePlayback(sentences: String?, editText: EditText)


    fun speak(sentences: List<String>, editText: EditText)

    fun pause()

    fun highlightSentence(start:Int, currentSentence: String, editText: EditText)
    fun destroyTTS()

    fun playFromClickPosition(start: Int)
    fun configTTS(preferences: AllPreferences)
}