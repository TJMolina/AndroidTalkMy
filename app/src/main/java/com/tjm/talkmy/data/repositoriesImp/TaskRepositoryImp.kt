package com.tjm.talkmy.data.repositoriesImp

import android.util.Log
import com.tjm.talkmy.data.database.dao.TasksDao
import com.tjm.talkmy.data.database.entities.toDatabase
import com.tjm.talkmy.domain.models.Task
import com.tjm.talkmy.domain.repositories.TasksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImp @Inject constructor(private val tasksDao: TasksDao) : TasksRepository {
    override suspend fun getTasksFromLocal(): Flow<List<Task>> {
        return tasksDao.getAllTasks().map { item ->
            item.map { task -> task.toDomain() }
        }
    }

    override suspend fun uploadTaskLocal(task: Task): String? {
        runCatching {
            tasksDao.insertTask(task.toDatabase())
        }
            .onSuccess { Log.i("uploadTaskLocal", "success") }
            .onFailure { Log.e("uploadTaskLocal", it.toString()) }
        return null
    }

    override suspend fun clearTasks() {
        runCatching { tasksDao.deleteAllTasks() }
            .onSuccess { Log.i("clearTasks", "success") }
            .onFailure { Log.e("clearTasks", it.toString()) }
    }

    override suspend fun getTask(id: String): Task? {
        runCatching {
            tasksDao.getTask(id)
        }
            .onSuccess { return it.toDomain() }
            .onFailure { Log.e("uploadTaskLocal", it.toString()) }
        return null
    }

    override suspend fun deleteTask(id: String) {
        runCatching {
            tasksDao.deleteTask(id)
        }
            .onSuccess { Log.i("TaskRepositoryImp deleteTask", "succefull") }
            .onFailure { Log.e("TaskRepositoryImp deleteTask", it.toString()) }
    }


}