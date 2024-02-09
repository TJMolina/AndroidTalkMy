package com.tjm.talkmy.ui.tasks.adapter

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tjm.talkmy.databinding.ItemTaskBinding
import com.tjm.talkmy.domain.models.Task

class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemTaskBinding.bind(view)

    fun render(
        task: Task,
        editTask: (String, String) -> Unit,
        deleteTask: (String, Int) -> Unit
    ) {
        val textSize = task.nota.length
        val maxSize25 = minOf(35, textSize) // Mínimo entre 35 y el tamaño del texto
        val maxSize150 = minOf(150, textSize) // Mínimo entre 150 y el tamaño del texto
        val auxText = task.nota.substring(0, maxSize25)

        val builder = SpannableStringBuilder(auxText)
        builder.setSpan(
            StyleSpan(android.graphics.Typeface.BOLD),
            0, // Inicio del rango
            auxText.length, // Fin del rango (inclusivo)
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.textBold25characters.text = builder
        binding.tvTask.text = task.nota.substring(auxText.length, maxSize150)

        binding.tvTask.setOnClickListener { editTask(task.id!!, task.nota) }
        binding.btnDeleteTask.setOnClickListener { deleteTask(task.id!!, adapterPosition) }
    }
}