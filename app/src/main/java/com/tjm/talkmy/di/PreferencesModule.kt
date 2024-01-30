package com.tjm.talkmy.di

import android.content.Context
import com.tjm.talkmy.data.repositoriesImp.PreferencesImp
import com.tjm.talkmy.data.source.preferences.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {
    @Singleton
    @Provides
    fun providePreferences(@ApplicationContext app: Context):Preferences = PreferencesImp(app)
}