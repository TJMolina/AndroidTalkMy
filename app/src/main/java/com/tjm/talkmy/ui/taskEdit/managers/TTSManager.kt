package com.tjm.talkmy.ui.taskEdit.managers

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.google.android.material.slider.Slider
import com.orhanobut.logger.Logger
import com.tjm.talkmy.domain.interfaces.TTSManagerInterface
import com.tjm.talkmy.domain.models.AllPreferences
import com.tjm.talkmy.ui.core.states.SpeakingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class TTSManager(
    private val tts: TextToSpeech,
    private val rangeSlider: Slider?
) :
    TTSManagerInterface {
    private var isPlaying = MutableStateFlow(SpeakingState())
    val _isPlaying: StateFlow<SpeakingState> = isPlaying
    val currentSentenceToHighlight = MutableStateFlow(-1)
    var sentences: List<String> = emptyList()
    var currentSentenceIndex = 0
    var changinParagraphWithControls = false
    override fun togglePlayback(
        listOfSentences: List<String>?,
        indice: Int?,
        play: Boolean?
    ) {
            if (play!! && !tts.isSpeaking) {
                reloadSentences(listOfSentences!!)
                if (indice!! >= 0) currentSentenceIndex = indice
                if (currentSentenceIndex > listOfSentences.size - 1) currentSentenceIndex =
                    listOfSentences.size - 1
                speak()
            } else if (!play && tts.isSpeaking) {
                pause()
            }
    }

    fun reloadSentences(listOfSentences: List<String>) {
        sentences = listOfSentences
    }

    override fun speak() {
        if (sentences.isNotEmpty() && currentSentenceIndex < sentences.size) {
            isPlaying.value = SpeakingState(isSpeaking = true)
            val currentSentence = sentences[currentSentenceIndex]
            tts?.apply {
                speak(
                    currentSentence,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    currentSentence
                )
                setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        rangeSlider?.value = currentSentenceIndex.toFloat()
                        currentSentenceToHighlight.value = currentSentenceIndex
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
            if ((!isPlaying.value.isSpeaking && !sentences.isNullOrEmpty() && indice < sentences.size) || changinParagraphWithControls) {
                currentSentenceIndex = indice
                currentSentenceToHighlight.value = indice
                if(changinParagraphWithControls && tts.isSpeaking) speak()
                changinParagraphWithControls = false
            }
        }
    }

    override fun findStartByAproxStart(start: Int, dirtySentences: String) {
        if (!isPlaying.value.isSpeaking) {
            if (currentSentenceIndex < 0) currentSentenceIndex = 0
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


