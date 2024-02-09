package com.tjm.talkmy.ui.taskEdit.managers

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.google.android.material.slider.Slider
import com.orhanobut.logger.Logger
import com.tjm.talkmy.R
import com.tjm.talkmy.domain.interfaces.TTSManagerInterface
import com.tjm.talkmy.domain.models.AllPreferences
import com.tjm.talkmy.domain.models.Sentence
import com.tjm.talkmy.ui.core.states.SpeakingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TTSManager(
    private val tts: TextToSpeech,
    private val rangeSlider: Slider?
) :
    TTSManagerInterface {
    private var isPlaying = MutableStateFlow(SpeakingState())
    val sentencesManager = SentencesManager()
    val _isPlaying: StateFlow<SpeakingState> = isPlaying
    val currentSentenceToHighlight = MutableStateFlow(Sentence("",0))
    var sentences: List<Sentence> = emptyList()
    var currentSentenceIndex = 0
    override fun togglePlayback(dirtySentences:String) {
        if (tts.isSpeaking) {
            pause()
        } else {
            reloadSentences(dirtySentences)
            if(currentSentenceIndex > sentences.size - 1) currentSentenceIndex = sentences.size - 1
            speak()
        }
    }
    fun reloadSentences(dirtySentences: String){
            sentences = sentencesManager.getSentences(dirtySentences)
    }

    override fun speak() {
        if (sentences.isNotEmpty() && currentSentenceIndex < sentences.size) {
            isPlaying.value = SpeakingState(isSpeaking = true)
            val currentSentence = sentences[currentSentenceIndex]
            tts?.apply {
                speak(
                    currentSentence.sentence,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    currentSentence.sentence
                )
                setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        rangeSlider?.value = currentSentenceIndex.toFloat()
                        currentSentenceToHighlight.value = currentSentence
                    }
                    override fun onDone(utteranceId: String?) {
                        if (currentSentenceIndex == sentences.size - 1) {
                            currentSentenceIndex = 0
                            isPlaying.value = SpeakingState(finalized = true)
                        } else {
                            currentSentenceIndex++
                            speak()
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


    suspend fun findStartByIndice(indice: Int) {
        return withContext(Dispatchers.Default) {
            if (!tts.isSpeaking && !sentences.isNullOrEmpty() && indice < sentences.size) {
                currentSentenceIndex = indice
                currentSentenceToHighlight.value = sentences[currentSentenceIndex]
            }
        }
    }

    override fun findStartByAproxStart(start: Int, dirtySentences: String) {
        if (!isPlaying.value.isSpeaking) {
            if (sentences.isNullOrEmpty()) reloadSentences(dirtySentences)
            currentSentenceIndex = sentences.indexOfLast { it.start <= start }
            if(currentSentenceIndex < 0) currentSentenceIndex = 0
        }
    }


    override fun destroyTTS() {
        isPlaying.value = SpeakingState(false)
        tts?.stop()
        tts?.shutdown()
    }

    override fun configTTS(preferences: AllPreferences) {
        val speechRate = preferences.speech
        val velocity = preferences.velocity
        val voice = preferences.voice
        val selectedVoice = tts.voices.firstOrNull { it.name == voice }
        if (selectedVoice != null) {
            tts.voice = selectedVoice
        }
        tts.setSpeechRate(velocity)
        tts.setPitch(speechRate)
    }
}


