package com.tjm.talkmy.ui.taskEdit

import android.widget.EditText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tjm.talkmy.data.source.preferences.Preferences
import com.tjm.talkmy.domain.models.AllPreferences
import com.tjm.talkmy.domain.models.FunctionName
import com.tjm.talkmy.domain.models.Task
import com.tjm.talkmy.domain.useCases.getTaskUseCase
import com.tjm.talkmy.domain.useCases.getTasksUseCase
import com.tjm.talkmy.domain.useCases.uploadTaskUseCasea
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
class ConfigsViewModel @Inject constructor(
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
                            if (preferences.value.readNextTask) getAllTasks(
                                function.id,
                                function.editText
                            ) else getTask(function.id, function.editText)
                        }

                        is FunctionName.SaveTask -> {
                            saveTask(function.text, function.id, function.fecha)
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

    private fun readNextsTaskFunction(editText: EditText, play: () -> Unit) {
        if (currentTask < allTasks.size - 1) {
            val nota = editText.text.toString()
            if (nota != allTasks[currentTask].nota) {
                val id = taskBeingEditing.id
                val fecha = taskBeingEditing.fecha
                executeFunction(FunctionName.SaveTask(nota, id, fecha))
                allTasks[currentTask].nota = nota
            }
            currentTask++
            taskBeingEditing = allTasks[currentTask]

            editText.setText(taskBeingEditing.nota)
            play()
        }
    }

    private fun getAllTasks(id: String?, edText: EditText) {
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

    private fun getTask(id: String?, edText: EditText) {
        if (!id.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                getTaskUseCase(id)?.let {
                    taskBeingEditing = it
                }
            }
        }
    }

    private fun saveTask(text: String, id: String? = null, fecha: String? = null) {
        if (text.isEmpty()) return
        val auxtask = Task(
            id = id ?: taskBeingEditing.id,
            nota = text,
            fecha = fecha ?: taskBeingEditing.fecha
        )
        CoroutineScope(Dispatchers.IO).launch {
            uploadTaskUseCasea(auxtask)
        }
    }


}
