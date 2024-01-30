package com.tjm.talkmy.domain.useCases

import com.tjm.talkmy.domain.repositories.TasksRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val tasksRepositoriy: TasksRepository
) {
    suspend operator fun invoke(id: String) = tasksRepositoriy.deleteTask(id)
}