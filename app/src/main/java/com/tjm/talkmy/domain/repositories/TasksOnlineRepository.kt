package com.tjm.talkmy.domain.repositories

interface TasksOnlineRepository {
    suspend fun getTextFromUrls(url:String): String?
}