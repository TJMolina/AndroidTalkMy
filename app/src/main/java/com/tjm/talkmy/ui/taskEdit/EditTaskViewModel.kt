package com.tjm.talkmy.ui.taskEdit

import android.widget.EditText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tjm.talkmy.core.ResponseState
import com.tjm.talkmy.domain.models.Task
import com.tjm.talkmy.domain.useCases.getTaskUseCase
import com.tjm.talkmy.domain.useCases.onlineUseCases.GetTextFromUrlUseCase
import com.tjm.talkmy.domain.useCases.uploadTaskUseCasea
import com.tjm.talkmy.ui.core.TTSManager
import com.tjm.talkmy.ui.core.states.LoadingErrorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EditTaskViewModel @Inject constructor(
    private val uploadTaskUseCasea: uploadTaskUseCasea,
    private val getTaskUseCase: getTaskUseCase,
    private val getTextFromUrlUseCase: GetTextFromUrlUseCase
) :
    ViewModel() {
    var taskBeingEditing = Task(nota = "")
    private var _getTextFromUrlProcces = MutableStateFlow(LoadingErrorState())
    val getTextFromUrlProcces: StateFlow<LoadingErrorState> = _getTextFromUrlProcces
    var textGotFromUrl: String? = null
    suspend fun saveTask(edText: EditText) {

        taskBeingEditing = Task(nota = edText.text.toString())

        if (taskBeingEditing.nota.isEmpty()) {
            return
        }

        uploadTaskUseCasea(taskBeingEditing!!)
    }

    fun getTask(id: String?, edText: EditText) {
        if (!id.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                getTaskUseCase(id)?.let { task ->
                    taskBeingEditing = task
                    withContext(Dispatchers.Main) { edText.setText(task.nota) }
                }
            }
        }
    }


    fun getTextFromUrl(url: String) = viewModelScope.launch(Dispatchers.IO) {
        getTextFromUrlUseCase(url).collect {
            when (it) {
                is ResponseState.Success -> {
                    textGotFromUrl = it.data
                    _getTextFromUrlProcces.value = LoadingErrorState(isLoading = false)
                }

                is ResponseState.Error -> {
                    _getTextFromUrlProcces.value = LoadingErrorState(error = "Error")
                }

                is ResponseState.Loading -> {
                    _getTextFromUrlProcces.value = LoadingErrorState(isLoading = true)
                }
            }
        }
    }


    fun getPositionClicked(editText: EditText, position: Int, ttsManager: TTSManager) {
        val text = editText.text.toString()

        // Buscar el índice del punto (.) más cercano antes y después del cursor
        val previousDotIndex = text.lastIndexOf(".", position)

        // Buscar el índice del salto de línea (\n) más cercano antes y después del cursor
        val previousNewLineIndex = text.lastIndexOf("\n", position)

        // Determinar cuál es el índice más cercano entre el punto (.) y el salto de línea (\n)
        val closestIndex = when {
            previousDotIndex == -1 && previousNewLineIndex == -1 -> -1
            previousDotIndex == -1 -> previousNewLineIndex
            previousNewLineIndex == -1 -> previousDotIndex
            else -> maxOf(previousDotIndex, previousNewLineIndex)
        }

        // Obtener la oración completa haciendo uso del índice más cercano
        val startIndex = closestIndex + 1

        ttsManager.playFromClickPosition(startIndex)
    }
}