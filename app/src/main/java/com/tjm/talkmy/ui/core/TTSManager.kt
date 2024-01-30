package com.tjm.talkmy.ui.core

import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.widget.EditText
import com.tjm.talkmy.domain.interfaces.TTSManagerInterface
import javax.inject.Singleton

class TTSManager(private val tts: TextToSpeech, private val editText: EditText)  : TTSManagerInterface{
    private var isPlaying: Boolean = false
    var currentSentenceIndex = 0

    override fun togglePlayback(sentences: List<String>?) {
        if (isPlaying) {
            pause()
        } else {
            speak(sentences)
        }
    }

    override fun speak(sentences: List<String>?) {
        if (!sentences.isNullOrEmpty() && currentSentenceIndex < sentences!!.size) {
            isPlaying = true
            val currentSentence = sentences!![currentSentenceIndex]
            tts?.apply {
                speak(currentSentence, TextToSpeech.QUEUE_FLUSH, null, currentSentence)
                setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        highlightSentence(currentSentence, editText)
                    }

                    override fun onDone(utteranceId: String?) {
                        currentSentenceIndex++
                        speak(sentences)
                    }

                    override fun onError(utteranceId: String?) {
                        Log.e("tts error:", "algun error al tratar de leer.")
                    }
                })
            }
        } else {
            currentSentenceIndex = 0
        }
    }

    override fun pause() {
        if (tts.isSpeaking) {
            tts.stop()
        }
        isPlaying = false
    }

    // Resaltar la oración actual
     override fun highlightSentence(currentSentence: String, editText: EditText) {
        val spannableString = SpannableString(editText.text.toString())
        val start = editText.text.toString().indexOf(currentSentence)
        val end = start + currentSentence.length
        spannableString.setSpan(
            BackgroundColorSpan(Color.YELLOW),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        editText.setText(spannableString)
    }

    override fun destroyTTS() {
        tts?.apply {
            stop()
            shutdown()
        }
    }
}

