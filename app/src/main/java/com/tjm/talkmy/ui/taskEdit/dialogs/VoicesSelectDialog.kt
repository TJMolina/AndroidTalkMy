package com.tjm.talkmy.ui.taskEdit.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.tjm.talkmy.databinding.DialogVoicesBinding
import com.tjm.talkmy.ui.taskEdit.DialogsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class VoicesSelectDialog : DialogFragment() {
    private var _binding: DialogVoicesBinding? = null
    val binding get() = _binding!!

    private var listener: ConfigSelectVoiceDialog? = null
    private val dialogsViewModel: DialogsViewModel by viewModels()

    interface ConfigSelectVoiceDialog {
        fun applyAllVoices(spiner: Spinner)
    }

    fun setListener(listener: ConfigSelectVoiceDialog) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        _binding = DialogVoicesBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        initListeners()
        listener?.applyAllVoices(binding.voiceSpinner)

        return dialog
    }


    fun initListeners() {
        binding.btnCancelVoicesDialog.setOnClickListener {
            dismiss()
        }
        binding.btnApplyVoicesDialog.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                dialogsViewModel.preferencesRepository.saveVoicePreference(
                    voiceName =
                    binding.voiceSpinner.selectedItem.toString()
                )
                withContext(Dispatchers.Main) {
                    dismiss()
                }
            }

        }
    }
}