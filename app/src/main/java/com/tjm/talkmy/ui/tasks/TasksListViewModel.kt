package com.tjm.talkmy.ui.tasks

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.Logger
import com.tjm.talkmy.data.source.preferences.Preferences
import com.tjm.talkmy.domain.models.AllPreferences
import com.tjm.talkmy.domain.useCases.DeleteTaskUseCase
import com.tjm.talkmy.domain.useCases.getTasksUseCase
import com.tjm.talkmy.ui.tasks.adapter.TaskAdapter
import com.tjm.talkmy.ui.tasks.dialog.DialogDeleteTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TasksListViewModel @Inject constructor(
    private val getTasksUseCase: getTasksUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    val preferencesRepository: Preferences,
) :
    ViewModel() {
    val haveTaskState = MutableStateFlow(false)
    var preferences = MutableStateFlow(AllPreferences())
    val deleteTaskDialog = DialogDeleteTask()

    @SuppressLint("NotifyDataSetChanged")
    fun getLocalTasks(taskAdapter: TaskAdapter) {
        viewModelScope.launch(Dispatchers.IO) {
            getTasksUseCase().collectLatest { newTasks ->
                if (newTasks.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        Logger.d("ha ocurrido un error.")
                    }
                    haveTaskState.value = false
                } else {
                    haveTaskState.value = true
                    val currentTasks = taskAdapter.taskList
                    val newSize = newTasks.size
                    val currentSize = currentTasks.size

                    if (newSize > currentSize) {
                        if (newSize - currentSize > 1 && currentSize > 1) {
                            taskAdapter.taskList.add(newTasks[newSize - 1])
                            withContext(Dispatchers.Main) {
                                taskAdapter.notifyItemInserted(newSize - 1)
                            }
                        } else {
                            taskAdapter.taskList = newTasks.toMutableList()
                            withContext(Dispatchers.Main) {
                                taskAdapter.notifyDataSetChanged()
                            }
                        }
                    } else if (currentSize > 0) {
                        for (i in 0 until minOf(currentSize, newSize)) {
                            val currentTask = currentTasks[i]
                            val newTask = newTasks[i]
                            if (currentTask.nota != newTask.nota) {
                                currentTask.nota = newTask.nota
                                withContext(Dispatchers.Main) {
                                    taskAdapter.notifyItemChanged(i)
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    fun deleteTask(
        id: String,
        position: Int,
        taskAdapter: TaskAdapter,
        parentFragmentManager: FragmentManager
    ) {
        deleteTaskDialog.delete = {
            taskAdapter.taskList.removeAt(position)
            taskAdapter.notifyItemRemoved(position)
            viewModelScope.launch(Dispatchers.IO) {
                deleteTaskUseCase(id)
            }
        }
        deleteTaskDialog.show(parentFragmentManager, "DeleteTaskDialog")
    }

    fun getAllPreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepository.getPreferences().collectLatest {
                preferences.value = it
            }
        }
    }
}