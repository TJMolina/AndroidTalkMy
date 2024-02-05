package com.tjm.talkmy.ui.taskEdit.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.Slider
import com.tjm.talkmy.databinding.DialogModifytextBinding

class TextOptionsDialog: DialogFragment() {
    private var _binding: DialogModifytextBinding? = null
    val binding get() = _binding!!

    private var listener: ConfigTextOptionsDialog? = null

    interface ConfigTextOptionsDialog {
        fun onApplyButtonClick(textSize:Float)
        fun configTextSize(textView: TextView, rangeSlider: Slider)
    }

    fun setListener(listener: ConfigTextOptionsDialog) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        _binding = DialogModifytextBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        listener?.configTextSize(binding.tvPreviTextDialog,binding.rsTextSizeTextDialog)
        initListeners()

        return dialog
    }


    fun initListeners() {
        binding.rsTextSizeTextDialog.addOnChangeListener { _, value, _ ->
            binding.tvPreviTextDialog.textSize = value
        }
        binding.btnCancelTextDialog.setOnClickListener {
            dismiss()
        }
        binding.btnApplyTextDialog.setOnClickListener {
            listener?.onApplyButtonClick(binding.rsTextSizeTextDialog.value)
            dismiss()
        }
    }
}