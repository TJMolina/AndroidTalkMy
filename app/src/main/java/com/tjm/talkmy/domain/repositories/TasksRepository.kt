package com.tjm.talkmy.domain.repositories

import com.tjm.talkmy.domain.models.Task
import kotlinx.coroutines.flow.Flow

interface TasksRepository {
    suspend fun getTasksFromLocal(): Flow<List<Task>>
    suspend fun uploadTaskLocal(task:Task): String?
    suspend fun clearTasks()
    suspend fun getTask(id:String):Task?
    suspend fun deleteTask(id:String)
}