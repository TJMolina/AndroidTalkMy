package com.tjm.talkmy.ui.taskEdit

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.tjm.talkmy.R
import com.tjm.talkmy.databinding.FragmentEditTaskBinding
import com.tjm.talkmy.domain.models.AllPreferences
import com.tjm.talkmy.domain.models.FunctionName
import com.tjm.talkmy.domain.models.Task
import com.tjm.talkmy.ui.core.states.SpeakingState
import com.tjm.talkmy.ui.taskEdit.managers.TTSManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@AndroidEntryPoint
class EditTaskFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentEditTaskBinding? = null
    private val binding get() = _binding!!

    private val dialogsViewModel by viewModels<DialogsViewModel>()
    private val editTaskViewModel by viewModels<EditTaskViewModel>()
    private lateinit var tts: TextToSpeech
    private lateinit var ttsManager: TTSManager
    private val arg: EditTaskFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        editTaskViewModel.getAllPreferences()
        dialogsViewModel.getAllPreferences()
        _binding = FragmentEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initUI()
    }

    private fun initUI() {
        reiveTask()
        observeTextSize()
        initEvents()
        initTTS()
        initMenu()
        initListeners()
        observeTextFromUrl()
        observeHightlight()
    }

    private fun initMenu() {
        val menuHost: MenuHost = binding.topAppBar
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                dialogsViewModel.createDialogs()
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.ivSave -> {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                        true
                    }

                    R.id.btnPageUrl -> {
                        dialogsViewModel.urlDialog.show(parentFragmentManager, "URLDialog")
                        true
                    }

                    R.id.optionsTalk -> {
                        dialogsViewModel.talkDialog.show(parentFragmentManager, "TalkOptionsDialog")
                        true
                    }

                    R.id.optionsVoices -> {
                        dialogsViewModel.voicesDialog.show(
                            parentFragmentManager,
                            "TalkOptionsDialog"
                        )
                        true
                    }

                    R.id.optionsTexto -> {
                        dialogsViewModel.textSizeDialog.show(
                            parentFragmentManager,
                            "TextSizeOptionsDialog"
                        )
                        true
                    }

                    else -> false
                }
            }
        })
    }

    private fun initTTS() {
        tts = TextToSpeech(requireContext(), this)
        ttsManager = TTSManager(tts, binding.rsTalkProgess)
        ttsManager.reloadSentences(binding.etTask.text.toString())
        reajustRangeSliderProgress(ttsManager.sentences.size.toFloat())
    }

    private fun initEvents() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            editTaskViewModel.executeFunction(FunctionName.SaveTask(binding.etTask.text.toString()))
            isEnabled = false
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListeners() {
        binding.apply {
            btnPlay.apply {
                isEnabled = false
                setOnClickListener { play() }
            }
            btnPause.apply {
                isEnabled = false
                setOnClickListener { play() }
            }
            rsTalkProgess.addOnChangeListener { slider, value, _ ->
                lifecycleScope.launch(Dispatchers.Default) {
                    ttsManager.findStartByIndice(value.toInt())
                }
            }
        }
        editTaskViewModel.executeFunction(FunctionName.ClickParagraph {
            binding.etTask.setOnClickListener {
                ttsManager.findStartByAproxStart(
                    binding.etTask.selectionStart,
                    binding.etTask.text.toString()
                )
            }
        })
    }

    private fun play() {
        ttsManager.togglePlayback(binding.etTask.text.toString())
        reajustRangeSliderProgress(ttsManager.sentences.size.toFloat() - 1)
    }

    private fun reajustRangeSliderProgress(toValue: Float) {
        if (toValue > 0) {
            binding.rsTalkProgess.valueTo = toValue
        }
    }

    fun reiveTask(url: String? = null) {
        val taskToEdit = try {
            arg.taskToEdit
        } catch (e: Exception) {
            null
        }
        //if isn't editing a note
        if (!taskToEdit.isNullOrEmpty()) {
            insertTextIntoEditText(arg.task!!, binding.etTask, arg.fontSize)
            lifecycleScope.launch(Dispatchers.IO) {
                editTaskViewModel.executeFunction(
                    FunctionName.GetTask(taskToEdit)
                )
            }

            //if received  an url from mainactivity
        } else {
            val urlAux = url ?: arguments?.getString("url")
            if (!urlAux.isNullOrEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    dialogsViewModel.getTextFromUrl(urlAux)
                }
            }
        }
    }

    private fun insertTextIntoEditText(
        text: String,
        editText: EditText,
        fontSize: Float? = null
    ) {
        if (fontSize != null) {
            editText.textSize = fontSize
        }
        editText.setSelection(0)
        requireActivity().runOnUiThread {
            editText.setText(text)
        }
    }

    private fun observeTextFromUrl() {
        lifecycleScope.launch(Dispatchers.IO) {
            dialogsViewModel.getTextFromUrlProcces.collect { value ->
                withContext(Dispatchers.Main) {
                    binding.apply {
                        if (value.isLoading) {
                            editTaskViewModel.executeFunction(FunctionName.SaveTask(binding.etTask.text.toString()))
                            circularProgressBar.visibility =
                                if (value.isLoading) View.VISIBLE else View.GONE
                            etTask.isEnabled = value.error.isBlank()
                        } else {
                            circularProgressBar.visibility = View.GONE
                            etTask.isEnabled = true
                            if (value.error.isBlank() && !dialogsViewModel.textGotFromUrl.isNullOrEmpty()) {
                                insertTextIntoEditText(
                                    dialogsViewModel.textGotFromUrl!!,
                                    binding.etTask
                                )
                                editTaskViewModel.taskBeingEditing = Task(nota = "")
                                editTaskViewModel.executeFunction(FunctionName.SaveTask(binding.etTask.text.toString()))
                            }

                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun observeIsPlaying() {
        lifecycleScope.launch(Dispatchers.IO) {
            ttsManager._isPlaying.collect { value ->
                if (value.isSpeaking) {
                    binding.etTask.setOnTouchListener { v, event -> true }
                    hideKeyboardAndSetUiState()
                } else if (value.error.isNotBlank()) {
                    showErrorToast()
                } else {
                    resetUiState()
                }
                executeNextTaskIfFinalized(value)
            }
        }
    }

    private suspend fun hideKeyboardAndSetUiState() {
        withContext(Dispatchers.Main) {
            hideKeyboard()
            updateUiForSpeakingState()
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.etTask.windowToken, 0)
        binding.etTask.clearFocus()
    }

    private fun updateUiForSpeakingState() {
        binding.apply {
            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
            rsTalkProgess.isEnabled = false
        }
    }

    private fun showErrorToast() {
        Toast.makeText(
            requireContext(),
            "Un error a ocurrido.",
            Toast.LENGTH_SHORT
        ).show()
    }

    @SuppressLint("ClickableViewAccessibility")
    private suspend fun resetUiState() {
        withContext(Dispatchers.Main) {
            binding.apply {
                etTask.setOnTouchListener(null)
                btnPlay.visibility = View.VISIBLE
                btnPause.visibility = View.INVISIBLE
                rsTalkProgess.isEnabled = true
            }
        }
    }

    private fun executeNextTaskIfFinalized(value: SpeakingState) {
        if (value.finalized && !value.isSpeaking) {
            editTaskViewModel.executeFunction(FunctionName.ReadNextTask(binding.etTask) { play() })
        }
    }

    private fun observeTextSize() {
        lifecycleScope.launch {
            dialogsViewModel.preferences.collect { textSize ->
                binding.etTask.textSize = textSize.textSize
            }
        }
    }

    override fun onInit(status: Int) {
        lifecycleScope.launch {
            dialogsViewModel.preferences.collectLatest { preferences ->
                handleTextToSpeechInitialization(status, preferences)
            }
        }
    }

    private suspend fun handleTextToSpeechInitialization(status: Int, preferences: AllPreferences) {
        if (status == TextToSpeech.SUCCESS) {
            val output = tts.setLanguage(Locale.getDefault())
            if (output == TextToSpeech.LANG_MISSING_DATA || output == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.i("yo", "Error con el idioma")
            } else {
                ttsManager.configTTS(preferences)
                updateUiForTTSInitialization()
                observeIsPlaying()
                dialogsViewModel.createSelectVoicesDialog(tts, requireContext())
            }
        } else {
            Log.i("yo", "Error con el TTS")
        }
    }

    private suspend fun updateUiForTTSInitialization() {
        withContext(Dispatchers.Main) {
            binding.btnPlay.isEnabled = true
            binding.btnPause.isEnabled = true
        }
    }


    private fun observeHightlight() {
        lifecycleScope.launch(Dispatchers.Main) {
            ttsManager.currentSentenceToHighlight.collect {
                binding.etTask.apply {
                    setSelection(it.start, it.start + it.sentence.length)
                    requestFocus()
                }
            }
        }
    }


    override fun onDestroy() {
        ttsManager.destroyTTS()
        super.onDestroy()
    }
}
