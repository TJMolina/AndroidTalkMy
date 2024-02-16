package com.tjm.talkmy.ui.taskEdit.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.orhanobut.logger.Logger
import com.tjm.talkmy.databinding.DialogInsertUrlBinding
import com.tjm.talkmy.ui.core.extensions.isURL
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UrlDialog : DialogFragment() {
    private var _binding: DialogInsertUrlBinding? = null
    val binding get() = _binding!!

    interface addFunctions {
        fun searchUrl(url: String)
    }

    lateinit var functions: addFunctions

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        _binding = DialogInsertUrlBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        initListeners()

        return dialog
    }

    fun initListeners() {
        binding.btnCloceDialog.setOnClickListener {
            dismiss()
        }
        binding.btnGetTextFromUrl.setOnClickListener {
            val url = binding.edUrl.text.toString().trim()
            if (url.isNotBlank()) {
                if (url.isURL()) {
                    functions.searchUrl(url)
                    dismiss()
                }
                binding.edUrl.setTextColor(Color.RED)
            }
        }
    }
}