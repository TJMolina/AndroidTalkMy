package com.tjm.talkmy.ui.taskEdit

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.tjm.talkmy.R
import com.tjm.talkmy.databinding.FragmentEditTaskBinding
import com.tjm.talkmy.ui.core.TTSManager
import com.tjm.talkmy.ui.core.extensions.isURL
import com.tjm.talkmy.ui.core.extensions.separateSentences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@AndroidEntryPoint
class EditTaskFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentEditTaskBinding? = null
    private val binding get() = _binding!!

    private val editTaskViewModel by viewModels<EditTaskViewModel>()

    private lateinit var tts: TextToSpeech
    private lateinit var ttsManager: TTSManager
    private var sentences: List<String>? = null

    private val arg: EditTaskFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initUI()
    }

    private fun initUI() {
        editTaskViewModel.getTask(arg.taskToEdit, binding.etTask)
        tts = TextToSpeech(requireContext(), this)
        ttsManager = TTSManager(tts, binding.etTask)
        initEvents()
        initListeners()
    }


    private fun initEvents() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            CoroutineScope(Dispatchers.IO).launch {
                editTaskViewModel.saveTask(binding.etTask)
                isEnabled = false
                withContext(Dispatchers.Main) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    private fun initListeners() {
        binding.ivSave.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.btnPlay.apply {
            isEnabled = false
            setOnClickListener { play() }
        }
        binding.btnPageUrl.setOnClickListener { showDialog() }
    }


    private fun play() {
        sentences = binding.etTask.text.toString().separateSentences()
        ttsManager.togglePlayback(sentences)
    }

    private fun getTextFromUrl(url:String) {
        editTaskViewModel.getTextFromUrl(url)
        lifecycleScope.launch(Dispatchers.IO) {
            editTaskViewModel.getTextFromUrlProcces.collectLatest { value ->
                withContext(Dispatchers.Main) {
                    if (value.isLoading) {

                    } else if (value.error.isNotBlank()) {

                    } else {
                        binding.etTask.setText(editTaskViewModel.textGotFromUrl)
                    }
                }
            }
        }
    }

    private fun showDialog(){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_insert_url)
        val btnGetTextFromUrl = dialog.findViewById<Button>(R.id.btnGetTextFromUrl)
        val edUrl = dialog.findViewById<EditText>(R.id.edUrl)
        val btnCloceDialog = dialog.findViewById<ImageButton>(R.id.btnCloceDialog)

        btnCloceDialog.setOnClickListener{
            dialog.hide()
        }
        btnGetTextFromUrl.setOnClickListener {
            val url = edUrl.text.toString()
            edUrl.setTextColor(if (url.isURL()) Color.BLACK else Color.RED)
            if (url.isNotBlank() && url.isURL()) {
                getTextFromUrl(url)
                dialog.hide()
            }
        }
        dialog.show()
    }


    override fun onInit(status: Int) {
    //funcion parte de TextToSpeech
        if (status == TextToSpeech.SUCCESS) {
            val output = tts?.setLanguage(Locale.getDefault())
            if (output == TextToSpeech.LANG_MISSING_DATA || output == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.i("yo", "error con lo del idioma")
            } else {
                binding.btnPlay.isEnabled = true
            }
        } else {
            Log.i("yo", "error con el tts")
        }
    }

    override fun onDestroy() {
        ttsManager.destroyTTS()
        super.onDestroy()
    }
}
