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
import com.tjm.talkmy.data.source.preferences.Preferences
import com.tjm.talkmy.domain.models.AllPreferences
import com.tjm.talkmy.ui.core.extensions.isURL
import com.tjm.talkmy.ui.taskEdit.dialogs.TalkOptionsDialog
import com.tjm.talkmy.ui.taskEdit.dialogs.TextOptionsDialog
import com.tjm.talkmy.ui.taskEdit.dialogs.VoicesSelectDialog
import com.tjm.talkmy.ui.taskEdit.managers.MyAudioManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DialogsViewModel @Inject constructor(
    val preferencesRepository: Preferences,
    val audioManager: MyAudioManager
) :
    ViewModel() {
    var preferences = MutableStateFlow(AllPreferences())
    lateinit var urlDialog: Dialog

    val textSizeDialog = TextOptionsDialog()
    val talkDialog = TalkOptionsDialog()
    val voicesDialog = VoicesSelectDialog()

    init {
        getAllPreferences()
    }

    fun createDialogs(context: Context, getTextFromUrl: (String) -> Unit) {
        createTalkDialog()
        createTextDialog()
        createURLDialog(context, getTextFromUrl)
    }

    fun getAllPreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepository.getPreferences().collectLatest {
                preferences.value = AllPreferences(
                    speech = it.speech,
                    textSize = it.textSize,
                    velocity = it.velocity,
                    volume = it.volume,
                    voice = it.voice
                )
            }
        }
    }


    fun createURLDialog(context: Context, getTextFromUrl: (String) -> Unit) {
        urlDialog = Dialog(context)
        urlDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        urlDialog.setContentView(R.layout.dialog_insert_url)

        val btnGetTextFromUrl = urlDialog.findViewById<Button>(R.id.btnGetTextFromUrl)
        val edUrl = urlDialog.findViewById<EditText>(R.id.edUrl)
        val btnCloceDialog = urlDialog.findViewById<ImageButton>(R.id.btnCloceDialog)

        btnCloceDialog.setOnClickListener {
            urlDialog.dismiss()
        }
        btnGetTextFromUrl.setOnClickListener {
            val url = edUrl.text.toString()
            edUrl.setTextColor(if (url.isURL()) Color.BLACK else Color.RED)
            if (url.isNotBlank() && url.isURL()) {
                urlDialog.dismiss()
                getTextFromUrl(url)
            }
        }
    }

     fun createSelectVoicesDialog(tts: TextToSpeech, context: Context) {
        voicesDialog.setListener(object : VoicesSelectDialog.ConfigSelectVoiceDialog {
            override fun onApplyButtonClick() {
                viewModelScope.launch(Dispatchers.IO) {
                    preferencesRepository.saveVoicePreference(
                        voiceName =
                        voicesDialog.binding.voiceSpinner.selectedItem.toString()
                    )
                    withContext(Dispatchers.Main) { //TODO recordar implementar al tts
                     }
                }
            }

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

    fun createTalkDialog() {
        talkDialog.setListener(object : TalkOptionsDialog.ConfigTalkOptionsDialog {
            override fun onApplyButtonClick(volume: Int, speech: Float, velocity: Float) {
                viewModelScope.launch(Dispatchers.IO) {
                    preferencesRepository.saveTalkPreferences(
                        volume = volume,
                        speech = speech,
                        velocity = velocity
                    )
                    withContext(Dispatchers.Main) { audioManager.increaseVolume(volume) }
                }
            }

            override fun configRangeSliders() {
                talkDialog.binding.rsTono.value = preferences.value.speech
                talkDialog.binding.rsVolument.value = preferences.value.volume.toFloat()
                talkDialog.binding.rsVelocity.value = preferences.value.velocity
            }
        })
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
}