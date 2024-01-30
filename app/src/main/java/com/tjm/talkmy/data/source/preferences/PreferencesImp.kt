package com.tjm.talkmy.data.repositoriesImp

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tjm.talkmy.data.source.preferences.Preferences
import com.tjm.talkmy.domain.models.Task
import kotlinx.coroutines.flow.first
import javax.inject.Inject

val Context.dataStore by preferencesDataStore(name = "TASKBD")

class PreferencesImp @Inject constructor(private val context: Context) : Preferences {


}