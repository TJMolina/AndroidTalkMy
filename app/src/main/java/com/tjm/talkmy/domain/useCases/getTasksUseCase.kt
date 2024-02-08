package com.tjm.talkmy.domain.useCases

import com.tjm.talkmy.domain.models.Task
import com.tjm.talkmy.domain.repositories.TasksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class getTasksUseCase @Inject constructor(
    private val tasksRepositoriy: TasksRepository
) {
    suspend operator fun invoke(): Flow<List<Task>> {
        return tasksRepositoriy.getTasksFromLocal()
    }
}