package com.tjm.talkmy.domain.useCases.onlineUseCases

import com.tjm.talkmy.core.ResponseState
import com.tjm.talkmy.domain.repositories.TasksOnlineRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.orhanobut.logger.Logger
import com.tjm.talkmy.ui.core.extensions.translateHTMLtoPlain

class GetTextFromUrlUseCase @Inject
constructor(private val tasksOnlineRepository: TasksOnlineRepository) {
    suspend operator fun invoke(url: String):Flow<ResponseState<String>> = flow {
        emit(ResponseState.Loading<String>())
        val textHTML = tasksOnlineRepository.getTextFromUrls(url)
        if(!textHTML.isNullOrEmpty()){
            emit(ResponseState.Success<String>(textHTML.translateHTMLtoPlain()))
        } else{
            emit(ResponseState.Error<String>("Error al recibir el texto."))
        }
    }
}