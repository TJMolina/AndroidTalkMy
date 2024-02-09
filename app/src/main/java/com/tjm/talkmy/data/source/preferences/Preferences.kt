package com.tjm.talkmy.data.source.preferences

import com.tjm.talkmy.domain.models.AllPreferences
import com.tjm.talkmy.domain.models.PreferencesType
import kotlinx.coroutines.flow.Flow


interface Preferences {
    suspend fun getPreferences(): Flow<AllPreferences>
    suspend fun saveTalkPreferences(volume: Int, speech: Float, velocity: Float)
    suspend fun saveTextSize(size:Float)
    suspend fun saveVoicePreference(voiceName:String)

    suspend fun saveBooleanPreference(preference:Boolean, name:PreferencesType)

}