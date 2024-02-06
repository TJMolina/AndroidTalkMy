package com.tjm.talkmy.ui.taskEdit.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.orhanobut.logger.Logger
import com.tjm.talkmy.databinding.DialogInsertUrlBinding
import com.tjm.talkmy.databinding.DialogTalkoptionsBinding
import com.tjm.talkmy.ui.core.extensions.isURL
import com.tjm.talkmy.ui.taskEdit.DialogsViewModel
import com.tjm.talkmy.ui.taskEdit.managers.MyAudioManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class UrlDialog : DialogFragment() {
    private var _binding: DialogInsertUrlBinding? = null
    val binding get() = _binding!!
    interface addFunctions{
        fun searchUrl(url:String)
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
        binding.btnCloceDialog.setOnClickListener{
            dismiss()
        }
        binding.btnGetTextFromUrl.setOnClickListener {
            val url = binding.edUrl.text.toString()
            binding.edUrl.setTextColor(if (url.isURL()) Color.BLACK else Color.RED)
            if (url.isNotBlank() && url.isURL()) {
                functions.searchUrl(url)
                dismiss()
            }
        }
    }
}