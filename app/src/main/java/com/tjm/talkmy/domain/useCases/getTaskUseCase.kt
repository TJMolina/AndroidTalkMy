package com.tjm.talkmy.domain.useCases

import com.tjm.talkmy.domain.models.Task
import com.tjm.talkmy.domain.repositories.TasksRepository
import javax.inject.Inject

class getTaskUseCase @Inject constructor(
    private val tasksRepositoriy: TasksRepository
) {
    suspend operator fun invoke(id:String): Task? {
        return tasksRepositoriy.getTask(id)
    }
}