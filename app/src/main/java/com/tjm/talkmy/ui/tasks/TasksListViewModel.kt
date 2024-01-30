package com.tjm.talkmy.ui.tasks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tjm.talkmy.domain.useCases.DeleteTaskUseCase
import com.tjm.talkmy.domain.useCases.getTasksUseCase
import com.tjm.talkmy.ui.tasks.adapter.TaskAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TasksListViewModel @Inject constructor(
    private val getTasksUseCase: getTasksUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) :
    ViewModel() {
    private var _state = MutableStateFlow(TasksState())
    val getTaskState: StateFlow<TasksState> = _state

    fun getLocalTasks() {

        viewModelScope.launch(Dispatchers.Main) {
            val response = withContext(Dispatchers.IO) { getTasksUseCase() }

            if(!response.isNullOrEmpty()){
                _state.value = TasksState(tasksList = response.toMutableList())
            }
            else{
                _state.value = TasksState(error = "ha ocurrido un error")
            }
        }
    }

    fun deleteTask(id: String, position: Int, taskAdapter: TaskAdapter) {
        viewModelScope.launch {
            deleteTaskUseCase(id)
        }
         _state.value.tasksList.removeAt(position)
        taskAdapter.notifyItemRemoved(position)
    }

}