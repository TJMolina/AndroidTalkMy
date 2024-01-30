package com.tjm.talkmy.ui.tasks

import com.tjm.talkmy.domain.models.Task

data class TasksState (
    val isLoading: Boolean = false,
    var tasksList:MutableList<Task> = emptyList<Task>().toMutableList(),
    val error: String = ""
)