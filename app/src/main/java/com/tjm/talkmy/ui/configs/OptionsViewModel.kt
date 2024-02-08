package com.tjm.talkmy.ui.configs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.Logger
import com.tjm.talkmy.data.source.preferences.Preferences
import com.tjm.talkmy.domain.models.AllPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OptionsViewModel @Inject constructor(private val preferencesRepository: Preferences) :
    ViewModel() {
    var preferences = MutableStateFlow(AllPreferences())

    init {
        getAllPreferences()
    }

    fun getAllPreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepository.getPreferences().collectLatest {
                preferences.value = it
            }
        }
    }
    fun saveReadNextTaskOption(save:Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepository.saveReadNextTask(save)
        }
    }
    fun saveTaskOnlineOption(save:Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepository.saveOnlineTask(save)
        }
    }
}