package com.tjm.talkmy.ui.configs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.Logger
import com.tjm.talkmy.data.source.preferences.Preferences
import com.tjm.talkmy.domain.models.AllPreferences
import com.tjm.talkmy.domain.models.PreferencesType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OptionsViewModel @Inject constructor(private val preferencesRepository: Preferences) :
    ViewModel() {
    suspend fun getAllPreferences() = preferencesRepository.getPreferences().first()

    fun savePreference(preference: Boolean, name: PreferencesType) {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepository.saveBooleanPreference(preference, name)
        }
    }
}