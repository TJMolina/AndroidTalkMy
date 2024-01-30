package com.tjm.talkmy.di

import com.tjm.talkmy.data.repositoriesImp.TaskRepositoryImp
import com.tjm.talkmy.domain.repositories.TasksRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class TasksRepositoyModule {
@Binds
abstract fun provideTasksRepository(impl:TaskRepositoryImp): TasksRepository
}