package com.tjm.talkmy.domain.useCases.onlineUseCases

import com.tjm.talkmy.core.ResponseState
import com.tjm.talkmy.core.extensions.separateSentencesInsertPTagWeb
import com.tjm.talkmy.domain.repositories.TasksOnlineRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.tjm.talkmy.core.extensions.translateHTMLtoPlain

class GetTextFromUrlUseCase @Inject
constructor(private val tasksOnlineRepository: TasksOnlineRepository) {
    suspend operator fun invoke(url: String):Flow<ResponseState<String>> = flow {
        emit(ResponseState.Loading())
        val textHTML = tasksOnlineRepository.getTextFromUrls(url)
        if (!textHTML.isNullOrEmpty()) {
            emit(ResponseState.Success(textHTML.translateHTMLtoPlain().separateSentencesInsertPTagWeb()))
        } else {
            emit(ResponseState.Error("Error al recibir el texto."))
        }
    }
}