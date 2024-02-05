package com.tjm.talkmy.ui.taskEdit.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.Slider
import com.tjm.talkmy.databinding.DialogModifytextBinding
import com.tjm.talkmy.databinding.DialogVoicesBinding

class VoicesSelectDialog : DialogFragment(){
private var _binding: DialogVoicesBinding? = null
val binding get() = _binding!!

private var listener: ConfigSelectVoiceDialog? = null

interface ConfigSelectVoiceDialog {
    fun onApplyButtonClick()
    fun applyAllVoices(spiner:Spinner)
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
        listener?.onApplyButtonClick()
        dismiss()
    }
}
}