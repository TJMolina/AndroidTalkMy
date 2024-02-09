package com.tjm.talkmy.data.repositoriesImp

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.orhanobut.logger.Logger
import com.tjm.talkmy.data.source.preferences.Preferences
import com.tjm.talkmy.domain.models.AllPreferences
import com.tjm.talkmy.domain.models.PreferencesType
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore by preferencesDataStore(name = "TASKBD")

class PreferencesImp @Inject constructor(private val context: Context) : Preferences {
    override suspend fun getPreferences() = context.dataStore.data.map {
        AllPreferences(
            textSize = it[floatPreferencesKey(PreferencesType.TEXTSIZE.name)] ?: 22.0f,
            volume = it[intPreferencesKey(PreferencesType.VOLUME.name)] ?: 10,
            speech = it[floatPreferencesKey(PreferencesType.SPEECH.name)] ?: 1f,
            velocity = it[floatPreferencesKey(PreferencesType.VELOCITY.name)] ?: 1f,
            voice = it[stringPreferencesKey(PreferencesType.VOICE.name)] ?: "",
            readNextTask = it[booleanPreferencesKey(PreferencesType.NEXTTASK.name)] ?: false,
            saveOnline = it[booleanPreferencesKey(PreferencesType.SAVEONLINE.name)] ?: false,
            clickParagraph = it[booleanPreferencesKey(PreferencesType.CLICKPARAGRAPH.name)]?: false
        )
    }

    override suspend fun saveTalkPreferences(volume: Int, speech: Float, velocity: Float) {
        runCatching {
            context.dataStore.edit { preferences ->
                preferences[intPreferencesKey(PreferencesType.VOLUME.name)] = volume
                preferences[floatPreferencesKey(PreferencesType.SPEECH.name)] = speech
                preferences[floatPreferencesKey(PreferencesType.VELOCITY.name)] = velocity

            }
        }
            .onSuccess { Logger.d("Success") }
            .onFailure { Logger.e("failure: $it") }
    }

    override suspend fun saveTextSize(size: Float) {
        runCatching {
            context.dataStore.edit { preferences ->
                preferences[floatPreferencesKey(PreferencesType.TEXTSIZE.name)] = size

            }
        }
            .onSuccess { Logger.d("Success") }
            .onFailure { Logger.e("failure: $it") }
    }

    override suspend fun saveVoicePreference(voiceName: String) {
        runCatching {
            context.dataStore.edit { preferences ->
                preferences[stringPreferencesKey(PreferencesType.VOICE.name)] = voiceName
            }
        }
            .onSuccess { Logger.d("Success") }
            .onFailure { Logger.e("failure: $it") }
    }


    override suspend fun saveBooleanPreference(preference:Boolean, name:PreferencesType){
        runCatching {
            context.dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(name.name)] = preference
            }
        }
            .onSuccess { Logger.d("Success") }
            .onFailure { Logger.e("failure: $it") }
    }
}