package com.tjm.talkmy.domain.useCases

import android.util.Log
import com.tjm.talkmy.domain.models.Task
import com.tjm.talkmy.domain.repositories.TasksRepository
import javax.inject.Inject

class getTasksUseCase @Inject constructor(
    private val tasksRepositoriy: TasksRepository
) {
    suspend operator fun invoke():List<Task>?{
        val tasks = tasksRepositoriy.getTasksFromLocal()
        return if(!tasks.isNullOrEmpty()) tasks else null
    }
}