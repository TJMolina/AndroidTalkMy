package com.tjm.talkmy.ui.taskEdit.managers

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.tjm.talkmy.R
import com.tjm.talkmy.domain.interfaces.TTSManagerInterface
import com.tjm.talkmy.ui.core.extensions.separateSentences
import com.tjm.talkmy.ui.core.states.SpeakingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class TTSManager(private val tts: TextToSpeech, private val context: FragmentActivity) :
    TTSManagerInterface {
    private var isPlaying = MutableStateFlow(SpeakingState())
    val _isPlaying: StateFlow<SpeakingState> = isPlaying
    private var playSince = 0
    private var sentences: List<String> = emptyList()
    private var currentSentenceIndex = 0
    override fun togglePlayback(sentences: String?, editText: EditText) {
        if (isPlaying.value.isSpeaking) {
            pause()
        } else {
            this.sentences = if (playSince > 0) {
                sentences?.substring(playSince)?.separateSentences().orEmpty()
            } else {
                sentences?.separateSentences().orEmpty()
            }
            if (this.sentences.isNotEmpty()) currentSentenceIndex = 0
            speak(this.sentences, editText)
        }
    }


    override fun speak(sentences: List<String>, editText: EditText) {
        if (sentences.isNotEmpty()) {
            isPlaying.value = SpeakingState(isSpeaking = true)
            val currentSentence = sentences[currentSentenceIndex]
            tts?.apply {
                speak(currentSentence, TextToSpeech.QUEUE_FLUSH, null, currentSentence)
                setOnUtteranceProgressListener(object : UtteranceProgressListener() {

                    override fun onStart(utteranceId: String?) {
                        playSince = editText.text.toString().indexOf(currentSentence, playSince)
                        highlightSentence(playSince, currentSentence, editText)
                        playSince += 1
                    }

                    override fun onDone(utteranceId: String?) {
                        if (currentSentenceIndex == sentences.size - 1) {
                            currentSentenceIndex = 0
                            playSince = 0
                            isPlaying.value = SpeakingState(isSpeaking = false)
                        } else {
                            currentSentenceIndex++
                            speak(sentences, editText)
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        Log.e("tts error:", "Some error occurred while trying to read.")
                    }
                })
            }
        }
    }


    override fun pause() {
        tts.stop()
        isPlaying.value = SpeakingState(isSpeaking = false)
    }

    override fun playFromClickPosition(start: Int) {
        if (!isPlaying.value.isSpeaking) {
            playSince = start
        }
    }

    // Resaltar la oración actual
    override fun highlightSentence(start: Int, currentSentence: String, editText: EditText) {
        val end = start + currentSentence.length
        val spannableString = SpannableString(editText.text.toString())
        spannableString.setSpan(
            BackgroundColorSpan(ContextCompat.getColor(context, R.color.highlit)),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        //resalto el texto
        context.runOnUiThread {
            editText.setText(spannableString)
        }
    }

    override fun destroyTTS() {
        isPlaying.value = SpeakingState(false)
        tts?.stop()
        tts?.shutdown()
    }
}


