package com.tjm.talkmy.domain.useCases

import com.tjm.talkmy.domain.models.Task
import com.tjm.talkmy.domain.repositories.TasksRepository
import javax.inject.Inject

class uploadTaskUseCasea @Inject constructor(private val tasksRepository: TasksRepository) {
 suspend operator fun invoke(task:Task) = tasksRepository.uploadTaskLocal(task)
}