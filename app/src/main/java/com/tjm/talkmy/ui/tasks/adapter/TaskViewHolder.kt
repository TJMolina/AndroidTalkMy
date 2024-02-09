package com.tjm.talkmy.ui.tasks.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tjm.talkmy.databinding.ItemTaskBinding
import com.tjm.talkmy.domain.models.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val binding = ItemTaskBinding.bind(view)
    fun render(
        task: Task,
        editTask: (String, String) -> Unit,
        deleteTask: (String, Int) -> Unit
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            val auxText = task.nota.substring(0, if(task.nota.length >= 150) 150 else task.nota.length)
            withContext(Dispatchers.Main) {
                binding.tvTask.text = auxText
                binding.tvTask.setOnClickListener { editTask(task.id!!, task.nota) }
                binding.btnDeleteTask.setOnClickListener { deleteTask(task.id!!, adapterPosition) }
            }
        }
    }
}