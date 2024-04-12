package com.tjm.talkmy.ui.taskEdit

import android.annotation.SuppressLint
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
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
import com.tjm.talkmy.ui.taskEdit.managers.WebViewManager
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
    private lateinit var editTextManager: WebViewManager
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
        initComplements()
        initEvents()
        initObservers()
        initListeners()

    }

    private fun initComplements() {
        initEditText()
        initTTS()
        initMenu()
    }

    private fun initObservers() {
        observeTextSize()
        observeTextFromUrl()
        observeHightlight()
    }

    private fun initEditText() {
        editTextManager = WebViewManager(binding.webView)
        editTextManager.loadHTML()
        binding.loadingText.visibility = View.VISIBLE
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                reiveTask()
                editTaskViewModel.executeFunction(FunctionName.ClickParagraph {
                    editTextManager.setParagraphClickedListener()
                })
                binding.loadingText.visibility = View.GONE
            }
        }
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
        editTextManager.getSentences { sentences, indice ->
            ttsManager.reloadSentences(sentences)
        }
        reajustRangeSliderProgress(ttsManager.sentences.size.toFloat())
    }


    private fun initEvents() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            editTextManager.text {
                editTaskViewModel.executeFunction(FunctionName.SaveTask(it))
            }
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
    }


    private fun play() {
        editTextManager.modifiedVerify {
            if (it == "false") {
                editTextManager.getSentences { sentences, indice ->
                    reajustRangeSliderProgress(sentences.size.toFloat() - 1)
                    ttsManager.togglePlayback(sentences, indice)
                }
            } else {
                editTextManager.reloadText { sentences, indice ->
                    reajustRangeSliderProgress(sentences.size.toFloat() - 1)
                    ttsManager.togglePlayback(sentences, indice)
                }
            }
        }
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
        if (!taskToEdit.isNullOrEmpty()) {
            //if isn't editing a note
            addOverEditingTask(taskToEdit)
        } else {
            //if received  an url from mainactivity
            addTaskTextFromUrl(url)
        }
        //inicio observers en caso de entrar a la app desde una pagina web
        initObservers()
        //reajusto cosas relacionadas a las horaciones si está editando una nota
        editTextManager.getSentences { sentences, indice ->
            ttsManager.reloadSentences(sentences)
            reajustRangeSliderProgress(sentences.size.toFloat() - 1)
        }
    }

    private fun addTaskTextFromUrl(url: String?) {
        val urlAux = url ?: arguments?.getString("url")
        if (!urlAux.isNullOrEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                dialogsViewModel.getTextFromUrl(urlAux)
            }
        }
    }

    private fun addOverEditingTask(taskToEdit: String) {
        insertTextIntoEditText(arg.task!!, arg.fontSize)
        lifecycleScope.launch(Dispatchers.IO) {
            editTaskViewModel.executeFunction(
                FunctionName.GetTask(taskToEdit)
            )
        }
    }

    private fun insertTextIntoEditText(
        text: String,
        fontSize: Float? = null
    ) {
        if (fontSize != null) {
            editTextManager.fontSize = fontSize
        }
        editTextManager.setText(text)
    }


    private fun observeTextFromUrl() {
        lifecycleScope.launch(Dispatchers.IO) {
            dialogsViewModel.getTextFromUrlProcces.collect { value ->
                withContext(Dispatchers.Main) {
                    binding.apply {
                        if (value.isLoading) {
                            editTextManager.text {
                                editTaskViewModel.executeFunction(FunctionName.SaveTask(it))
                            }
                            circularProgressBar.visibility =
                                if (value.isLoading) View.VISIBLE else View.GONE
                            //etTask.isEnabled = value.error.isBlank()
                        } else {
                            circularProgressBar.visibility = View.GONE
                            //etTask.isEnabled = true
                            if (value.error.isBlank() && !dialogsViewModel.textGotFromUrl.isNullOrEmpty()) {
                                insertTextIntoEditText(dialogsViewModel.textGotFromUrl!!)
                                editTaskViewModel.taskBeingEditing = Task(nota = "")
                                editTextManager.text {
                                    editTaskViewModel.executeFunction(FunctionName.SaveTask(it))
                                }
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
                    updateUiForSpeakingState()
                } else if (value.error.isNotBlank()) {
                    showErrorToast()
                } else {
                    resetUiState()
                }
                executeNextTaskIfFinalized(value)
            }
        }
    }

    private suspend fun updateUiForSpeakingState() {
        withContext(Dispatchers.Main) {
            binding.apply {
                btnPlay.visibility = View.INVISIBLE
                btnPause.visibility = View.VISIBLE
                rsTalkProgess.isEnabled = false
            }
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
                btnPlay.visibility = View.VISIBLE
                btnPause.visibility = View.INVISIBLE
                rsTalkProgess.isEnabled = true
            }
        }
    }


    private fun executeNextTaskIfFinalized(value: SpeakingState) {
        if (value.finalized && !value.isSpeaking) {
            editTaskViewModel.executeFunction(FunctionName.ReadNextTask(editTextManager) { play() })
        }
    }


    private fun observeTextSize() {
        lifecycleScope.launch {
            dialogsViewModel.preferences.collect { preferences ->
                editTextManager.fontSize = preferences.textSize
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
                editTextManager.setSelection(if (it > 0) it else 0)
            }
        }
    }


    override fun onDestroy() {
        ttsManager.destroyTTS()
        super.onDestroy()
    }
}
