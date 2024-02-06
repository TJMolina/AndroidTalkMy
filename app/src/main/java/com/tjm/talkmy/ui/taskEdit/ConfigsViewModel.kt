package com.tjm.talkmy.ui.taskEdit

import android.util.Log
import android.widget.EditText
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
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

    val readNextsTask = true
    val saveOnline = false

    init {
        getAllPreferences()
    }

    fun getAllPreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepository.getPreferences().collectLatest {
                preferences.value = AllPreferences(
                    speech = it.speech,
                    textSize = it.textSize,
                    velocity = it.velocity,
                    volume = it.volume,
                    voice = it.voice
                )
            }
        }
    }

    fun executeFunction(function: FunctionName) {
        when (function) {
            is FunctionName.GetTask -> {
                if (readNextsTask) getAllTasks(
                    function.id,
                    function.editText
                ) else getTask(function.id, function.editText)
            }

            is FunctionName.SaveTask -> {
                viewModelScope.launch(Dispatchers.IO) {
                    saveTask(function.editText)
                    if (saveOnline) {
                        //TODO aqui subiria la nota a la bd
                    }
                }
            }

            is FunctionName.ReadNextTask -> {
                if (readNextsTask) {
                    readNextsTaskFunction(function.editText, function.play)
                }
            }

        }
    }

    private fun readNextsTaskFunction(editText: EditText, play: () -> Unit) {
        if (allTasks.size - 1 > currentTask) {
            if (editText.text.toString() != taskBeingEditing.nota) {
                executeFunction(FunctionName.SaveTask(editText))
            }
            currentTask++
            taskBeingEditing = allTasks[currentTask]
            editText.setText(taskBeingEditing.nota)
            play()
        }
    }

    fun getAllTasks(id: String?, edText: EditText) {
        if (!id.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                getTasksUseCase()?.let {
                    allTasks = it.toMutableList()
                    val indice =
                        if (allTasks.size <= 1) 0 else it.indexOfFirst { task -> task.id == id }
                    currentTask = indice
                    taskBeingEditing = allTasks[indice]!!
                    withContext(Dispatchers.Main) { edText.setText(taskBeingEditing.nota) }
                }
            }
        }
    }

    fun getTask(id: String?, edText: EditText) {
        if (!id.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                getTaskUseCase(id)?.let {
                    taskBeingEditing = it
                    withContext(Dispatchers.Main) { edText.setText(taskBeingEditing.nota) }
                }
            }
        }
    }

    suspend fun saveTask(edText: EditText) {
        val auxtask = Task(
            id = taskBeingEditing.id,
            nota = edText.text.toString(),
            fecha = taskBeingEditing.fecha
        )
        if (auxtask.nota.isEmpty()) {
            return

        }
        uploadTaskUseCasea(auxtask)
    }
}