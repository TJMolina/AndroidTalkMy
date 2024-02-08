package com.tjm.talkmy.ui.tasks

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.Logger
import com.tjm.talkmy.domain.models.Task
import com.tjm.talkmy.domain.useCases.DeleteTaskUseCase
import com.tjm.talkmy.domain.useCases.getTasksUseCase
import com.tjm.talkmy.ui.tasks.adapter.TaskAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TasksListViewModel @Inject constructor(
    private val getTasksUseCase: getTasksUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) :
    ViewModel() {
    var recivedTask = mutableListOf<Task>()
    var _recivedTask = MutableStateFlow(TasksState())

    @SuppressLint("NotifyDataSetChanged")
    fun getLocalTasks(taskAdapter: TaskAdapter) {
        viewModelScope.launch(Dispatchers.IO) {
            getTasksUseCase().collect { newTasks ->
                if (newTasks.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        _recivedTask.value = TasksState(error = "Ha ocurrido un error")
                    }
                } else {
                    val currentTasks = taskAdapter.taskList
                    val newSize = newTasks.size
                    val currentSize = currentTasks.size

                    if (newSize > currentSize) {
                        if (newSize - currentSize > 1 && currentSize > 1) {
                            Logger.d("Se añadió una nota")
                            withContext(Dispatchers.Main) {
                                taskAdapter.taskList.add(newTasks[newSize - 1])
                                taskAdapter.notifyItemInserted(newSize - 1)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                taskAdapter.taskList = newTasks.toMutableList()
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

    fun deleteTask(id: String, position: Int, taskAdapter: TaskAdapter) {
        taskAdapter.taskList.removeAt(position)
        taskAdapter.notifyItemRemoved(position)
        viewModelScope.launch {
            deleteTaskUseCase(id)
        }
    }

}