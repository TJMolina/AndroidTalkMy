package com.tjm.talkmy.ui.taskEdit.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.tjm.talkmy.databinding.DialogTalkoptionsBinding


class TalkOptionsDialog() : DialogFragment(){
    private var _binding: DialogTalkoptionsBinding? = null
    val binding get() = _binding!!

    private var listener: ConfigTalkOptionsDialog? = null

    interface ConfigTalkOptionsDialog {
        fun onApplyButtonClick(volume:Int, speech:Float,velocity:Float)
        fun configRangeSliders()

    }

    fun setListener(listener: ConfigTalkOptionsDialog) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        _binding = DialogTalkoptionsBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        listener?.configRangeSliders()
        initListeners()

        return dialog
    }


    fun initListeners() {
        binding.btnCancelTalkOption.setOnClickListener {
            dismiss()
        }
        binding.btnApplyTalkOption.setOnClickListener {
            listener?.onApplyButtonClick(
                volume = binding.rsVolument.value.toInt(),
                speech = binding.rsTono.value,
                velocity = binding.rsVelocity.value
            )
            dismiss()
        }
    }
}