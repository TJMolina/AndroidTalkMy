package com.tjm.talkmy.ui.tasks.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tjm.talkmy.databinding.ItemTaskBinding
import com.tjm.talkmy.domain.models.Task


class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val binding = ItemTaskBinding.bind(view)
    fun render(
        task: Task,
        editTask: (String) -> Unit,
        deleteTask: (String, Int) -> Unit
    ) {
        binding.tvTask.text = task.nota
        binding.tvTask.setOnClickListener { editTask(task.id!!) }
        binding.btnDeleteTask.setOnClickListener { deleteTask(task.id!!,adapterPosition) }
    }
}