package com.tjm.talkmy.data.source.preferences

import com.tjm.talkmy.domain.models.AllPreferences
import kotlinx.coroutines.flow.Flow


interface Preferences {
    suspend fun getPreferences(): Flow<AllPreferences>
    suspend fun saveTalkPreferences(volume: Int, speech: Float, velocity: Float)
    suspend fun saveTextSize(size:Float)

    suspend fun saveVoicePreference(voiceName:String)
}