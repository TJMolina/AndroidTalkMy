package com.tjm.talkmy.ui.taskEdit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.Logger
import com.tjm.talkmy.data.source.preferences.Preferences
import com.tjm.talkmy.domain.models.AllPreferences
import com.tjm.talkmy.domain.models.FunctionName
import com.tjm.talkmy.domain.models.Task
import com.tjm.talkmy.domain.useCases.getTaskUseCase
import com.tjm.talkmy.domain.useCases.getTasksUseCase
import com.tjm.talkmy.domain.useCases.uploadTaskUseCasea
import com.tjm.talkmy.ui.taskEdit.managers.WebViewManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EditTaskViewModel @Inject constructor(
    val preferencesRepository: Preferences,
    private val getTaskUseCase: getTaskUseCase,
    private val getTasksUseCase: getTasksUseCase,
    private val uploadTaskUseCasea: uploadTaskUseCasea

) :
    ViewModel() {
    var preferences = MutableStateFlow(AllPreferences())
    var taskBeingEditing = Task(nota = "")
    var allTasks: MutableList<Task> = emptyList<Task>().toMutableList()
    var currentTask = 0

    fun getAllPreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepository.getPreferences().collectLatest {
                preferences.value = it
            }
        }
    }

    fun executeFunction(function: FunctionName) {
        CoroutineScope(Dispatchers.IO).launch {
            preferences.collect {
                withContext(Dispatchers.Main) {
                    when (function) {
                        is FunctionName.GetTask -> {
                            if (preferences.value.readNextTask) getAllTasks(function.id) else getTask(
                                function.id
                            )
                        }

                        is FunctionName.SaveTask -> {
                            saveTask(function.text)
                            function.readNextTask()
                            if (preferences.value.saveOnline) {
                                //TODO aqui subiria la nota a la bd
                            }
                        }

                        is FunctionName.ReadNextTask -> {
                            if (preferences.value.readNextTask) {
                                readNextsTaskFunction(function.editText, function.play)
                            }
                        }

                        is FunctionName.ClickParagraph -> {
                            if (preferences.value.clickParagraph) function.function()
                        }
                    }
                }
            }
        }
    }

    private fun readNextsTaskFunction(editText: WebViewManager, play: () -> Unit) {
        if (currentTask < allTasks.size - 1) {
            editText.text { nota ->
                if (nota != allTasks[currentTask].nota) {
                    executeFunction(FunctionName.SaveTask(nota) { increaseTask(editText, play) })
                } else {
                    increaseTask(editText, play)
                }

            }
        }
    }

    private fun increaseTask(editText: WebViewManager, play: () -> Unit) {
        currentTask++
        taskBeingEditing = allTasks[currentTask]
        editText.setText(taskBeingEditing.nota)
        play()
    }

    private fun getAllTasks(id: String?) {
        if (!id.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                val tasks = getTasksUseCase().first()
                if (tasks.isNotEmpty()) {
                    allTasks = tasks.reversed().toMutableList()
                    currentTask = allTasks.indexOfFirst { task -> task.id == id }
                    taskBeingEditing = allTasks[currentTask]
                }
            }
        }
    }

    private fun getTask(id: String?) {
        if (!id.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                getTaskUseCase(id)?.let {
                    taskBeingEditing = it
                }
            }
        }
    }

    private fun saveTask(text: String) {
        Logger.d(text)
        if (text.isEmpty()) return
        taskBeingEditing.nota = text
        CoroutineScope(Dispatchers.IO).launch { uploadTaskUseCasea(taskBeingEditing) }
    }
}
