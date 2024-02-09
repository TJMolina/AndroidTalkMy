package com.tjm.talkmy.ui.taskEdit

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.speech.tts.TextToSpeech
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.slider.Slider
import com.orhanobut.logger.Logger
import com.tjm.talkmy.R
import com.tjm.talkmy.core.ResponseState
import com.tjm.talkmy.data.source.preferences.Preferences
import com.tjm.talkmy.domain.models.AllPreferences
import com.tjm.talkmy.domain.useCases.onlineUseCases.GetTextFromUrlUseCase
import com.tjm.talkmy.ui.core.extensions.isURL
import com.tjm.talkmy.ui.core.states.LoadingErrorState
import com.tjm.talkmy.ui.taskEdit.dialogs.TalkOptionsDialog
import com.tjm.talkmy.ui.taskEdit.dialogs.TextOptionsDialog
import com.tjm.talkmy.ui.taskEdit.dialogs.UrlDialog
import com.tjm.talkmy.ui.taskEdit.dialogs.VoicesSelectDialog
import com.tjm.talkmy.ui.taskEdit.managers.MyAudioManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DialogsViewModel @Inject constructor(
    val preferencesRepository: Preferences,
    private val getTextFromUrlUseCase: GetTextFromUrlUseCase

) :
    ViewModel() {
    var preferences = MutableStateFlow(AllPreferences())
    val textSizeDialog = TextOptionsDialog()
    val talkDialog = TalkOptionsDialog()
    val voicesDialog = VoicesSelectDialog()
    val urlDialog = UrlDialog()
    private var _getTextFromUrlProcces = MutableStateFlow(LoadingErrorState())
    val getTextFromUrlProcces: StateFlow<LoadingErrorState> = _getTextFromUrlProcces
    var textGotFromUrl: String? = null

    fun createDialogs() {
        createTextDialog()
        configUrlDialog()
    }

    fun getAllPreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepository.getPreferences().collectLatest {
                preferences.value = it
            }
        }
    }
    fun createTextDialog() {
        textSizeDialog.setListener(object : TextOptionsDialog.ConfigTextOptionsDialog {
            override fun onApplyButtonClick(textSize: Float) {
                viewModelScope.launch(Dispatchers.IO) {
                    preferencesRepository.saveTextSize(size = textSize)
                }
            }

            override fun configTextSize(textView: TextView, rangeSlider: Slider) {
                rangeSlider.value = preferences.value.textSize
                textView.textSize = preferences.value.textSize
            }
        })
    }

     fun createSelectVoicesDialog(tts: TextToSpeech, context: Context) {
        voicesDialog.setListener(object : VoicesSelectDialog.ConfigSelectVoiceDialog {
            override fun applyAllVoices(spiner: Spinner) {
                val voiceNames = mutableListOf<String>()
                val voices = tts.voices
                for (voice in voices) {
                    voiceNames.add(voice.name)
                }
                val adapter =
                    ArrayAdapter(context, android.R.layout.simple_spinner_item, voiceNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spiner.adapter = adapter
            }

        })
    }

    fun configUrlDialog(){
        urlDialog.functions = object : UrlDialog.addFunctions{
            override fun searchUrl(url: String) {
                getTextFromUrl(url)
            }
        }
    }

    fun getTextFromUrl(url: String) = viewModelScope.launch(Dispatchers.IO) {
        getTextFromUrlUseCase(url).collect {
            when (it) {
                is ResponseState.Success -> {
                    textGotFromUrl = it.data
                    _getTextFromUrlProcces.value = LoadingErrorState(isLoading = false)
                }

                is ResponseState.Error -> {
                    _getTextFromUrlProcces.value = LoadingErrorState(error = it.toString())
                }

                is ResponseState.Loading -> {
                    _getTextFromUrlProcces.value = LoadingErrorState(isLoading = true)
                }
            }
        }
    }
}