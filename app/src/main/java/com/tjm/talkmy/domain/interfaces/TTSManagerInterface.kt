package com.tjm.talkmy.domain.interfaces

import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.widget.EditText

interface TTSManagerInterface {
    fun togglePlayback(sentences: List<String>?)

    fun speak(sentences: List<String>?)

    fun pause()

    fun highlightSentence(currentSentence: String, editText: EditText)

    fun destroyTTS()
}