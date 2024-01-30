package com.tjm.talkmy.data.repositoriesImp

import android.util.Log
import com.tjm.talkmy.data.network.TaskApiService
import com.tjm.talkmy.domain.repositories.TasksOnlineRepository
import javax.inject.Inject

class TasksOnlineRepositoryImp @Inject constructor(private val apiService: TaskApiService) :
    TasksOnlineRepository {
    override suspend fun getTextFromUrls(url: String): String? {
        runCatching {
            apiService.getFromUrl(url)
        }
            .onSuccess { return it.body() }
            .onFailure { Log.e("taskonlinerepositoryimp getfromurl", it.message.toString()) }
        return null
    }
}