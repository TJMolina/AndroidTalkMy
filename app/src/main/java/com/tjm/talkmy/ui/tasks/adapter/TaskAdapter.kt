package com.tjm.talkmy.ui.tasks.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tjm.talkmy.R
import com.tjm.talkmy.domain.models.Task

class TaskAdapter(
    var taskList: MutableList<Task> = mutableListOf(),
    private val editTask: (String, String)->(Unit),
    private val deleteTask:(String, Int)->(Unit)
) : RecyclerView.Adapter<TaskViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TaskViewHolder(layoutInflater.inflate(R.layout.item_task, parent, false))
    }

    override fun getItemCount() = taskList.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val item = taskList[position]
        holder.render(item, editTask, deleteTask)
    }
}