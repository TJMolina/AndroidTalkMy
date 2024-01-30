package com.tjm.talkmy.ui.taskEdit

import android.widget.EditText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tjm.talkmy.core.ResponseState
import com.tjm.talkmy.domain.models.Task
import com.tjm.talkmy.domain.useCases.getTaskUseCase
import com.tjm.talkmy.domain.useCases.onlineUseCases.GetTextFromUrlUseCase
import com.tjm.talkmy.domain.useCases.uploadTaskUseCasea
import com.tjm.talkmy.ui.core.states.LoadingErrorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EditTaskViewModel @Inject constructor(
    private val uploadTaskUseCasea: uploadTaskUseCasea,
    private val getTaskUseCase: getTaskUseCase,
    private val getTextFromUrlUseCase: GetTextFromUrlUseCase
) :
    ViewModel() {
    private var taskBeingEditing: Task? = null
    private var _getTextFromUrlProcces = MutableStateFlow(LoadingErrorState())
    val getTextFromUrlProcces: StateFlow<LoadingErrorState> = _getTextFromUrlProcces
    var textGotFromUrl: String? = null
    suspend fun saveTask(edText: EditText) {
        if (edText.text.isBlank()) {
            return
        }

        val task = if (taskBeingEditing != null) {
            taskBeingEditing!!.nota = edText.text.toString()
            taskBeingEditing!!
        } else {
            Task(nota = edText.text.toString())
        }

        return withContext(Dispatchers.IO) {
            uploadTaskUseCasea(task)
        }
    }

    fun getTask(id: String?, edText: EditText) {
        if (!id.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                val taskToEdit = getTaskUseCase(id)
                if (taskToEdit != null) {
                    taskBeingEditing = taskToEdit
                    withContext(Dispatchers.Main) { edText.setText(taskToEdit.nota) }
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

}