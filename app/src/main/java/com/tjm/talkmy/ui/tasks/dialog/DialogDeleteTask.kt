package com.tjm.talkmy.ui.tasks.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.slider.Slider
import com.tjm.talkmy.databinding.DialogDeleteBinding
import com.tjm.talkmy.databinding.DialogModifytextBinding
import com.tjm.talkmy.ui.tasks.adapter.TaskAdapter
import kotlinx.coroutines.Dispatchers

class DialogDeleteTask: DialogFragment() {
    private var _binding: DialogDeleteBinding? = null
    val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        _binding = DialogDeleteBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        initListeners()
        return dialog
    }

    var delete:()->Unit = {}

    fun initListeners() {
        binding.btnCancelDeleteTaskDialog.setOnClickListener {
            dismiss()
        }
        binding.btnApplyDeleteTaskDialog.setOnClickListener {
            delete()
            dismiss()
        }
    }
}