package com.tjm.talkmy.ui.taskEdit.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.orhanobut.logger.Logger
import com.tjm.talkmy.databinding.DialogTalkoptionsBinding
import com.tjm.talkmy.ui.taskEdit.DialogsViewModel
import com.tjm.talkmy.ui.taskEdit.managers.MyAudioManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class TalkOptionsDialog : DialogFragment() {
    private var _binding: DialogTalkoptionsBinding? = null
    val binding get() = _binding!!


    private val dialogsViewModel: DialogsViewModel by viewModels()

    @Inject
    lateinit var audioManager: MyAudioManager

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        _binding = DialogTalkoptionsBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        observePreferences()
        initListeners()

        return dialog
    }

    private fun observePreferences() {
        lifecycleScope.launch(Dispatchers.IO) {
            dialogsViewModel.preferences.collect {
                binding.rsTono.value = it.speech
                binding.rsVolument.value = it.volume.toFloat()
                binding.rsVelocity.value = it.velocity
            }
        }
    }


    fun initListeners() {
        binding.btnCancelTalkOption.setOnClickListener {
            Logger.d(dialogsViewModel.preferences.value.toString())
            dismiss()
        }
        binding.btnApplyTalkOption.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                dialogsViewModel.preferencesRepository.saveTalkPreferences(
                    volume = binding.rsVolument.value.toInt(),
                    speech = binding.rsTono.value,
                    velocity = binding.rsVelocity.value
                )
                withContext(Dispatchers.Main) {
                    audioManager.increaseVolume(binding.rsVolument.value.toInt())
                    dismiss()
                }
            }
        }
    }
}