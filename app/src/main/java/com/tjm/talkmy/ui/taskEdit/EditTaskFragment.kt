package com.tjm.talkmy.ui.taskEdit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.core.content.ContextCompat.RECEIVER_EXPORTED
import androidx.core.content.ContextCompat.registerReceiver
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.orhanobut.logger.Logger
import com.tjm.talkmy.R
import com.tjm.talkmy.core.extensions.separateSentencesInsertPTag
import com.tjm.talkmy.databinding.FragmentEditTaskBinding
import com.tjm.talkmy.domain.models.AllPreferences
import com.tjm.talkmy.domain.models.FunctionName
import com.tjm.talkmy.domain.models.Task
import com.tjm.talkmy.ui.core.states.SpeakingState
import com.tjm.talkmy.ui.mediaPlayerNotification.MediaPlayerNotification
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
    lateinit var mediaPlayerNotification: MediaPlayerNotification
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                IntentActions.PLAY -> play(true)
                IntentActions.PAUSE -> play(false)
                IntentActions.NEXT -> next()
                IntentActions.PREV -> previus()
            }
        }
    }

    object IntentActions {
        const val PLAY = "PLAY"
        const val PAUSE = "PAUSE"
        const val NEXT = "NEXT"
        const val PREV = "PREV"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        editTaskViewModel.getAllPreferences()
        dialogsViewModel.getAllPreferences()
        _binding = FragmentEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(requireContext(), receiver, IntentFilter().apply {
            addAction(IntentActions.PLAY)
            addAction(IntentActions.PAUSE)
            addAction(IntentActions.NEXT)
            addAction(IntentActions.PREV)
        }, RECEIVER_EXPORTED)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = initEditText()

    private fun initEditText() {
        editTextManager = WebViewManager(binding.webView, requireContext())
        editTextManager.loadHTML("file:///android_asset/edittext.html")
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                editTextManager.setFontColor()
                binding.loadingText.visibility = View.GONE
                reiveTask()
                initUI()
                editTaskViewModel.executeFunction(FunctionName.ClickParagraph { editTextManager.setParagraphClickedListener() })
                editTextManager.initExtraListeners()
            }
        }
    }

    private fun initUI() {
        initComplements()
        initListeners()
        initObservers()
    }

    private fun initComplements() {
        initMediaPlayerNotification()
        initTTS()
        initMenu()
    }

    private fun initObservers() {
        observeTextSize()
        observeTextFromUrl()
        observeHightlight()
    }

    private fun initMediaPlayerNotification() {
        mediaPlayerNotification = MediaPlayerNotification()
        try {
            Intent(requireContext().applicationContext, MediaPlayerNotification::class.java).also {
                it.action = "START"
                requireContext().startService(it)
            }
        } catch (e: Exception) {
            Logger.e(e.toString())
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

    private fun initListeners() {
        binding.apply {
            btnPlay.apply {
                isEnabled = false
                setOnClickListener {
                    play(true)
                    activeNotification()
                }
            }
            btnPause.apply {
                isEnabled = false
                setOnClickListener {
                    play(false)
                    closeNotificationPlayer(true)
                }
            }
            rsTalkProgess.addOnChangeListener { slider, value, _ ->
                lifecycleScope.launch(Dispatchers.Default) {
                    ttsManager.findStartByIndice(value.toInt())
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            editTextManager.modifiedVerify {verify->
                if (verify == "false") {
                    editTextManager.text { text ->
                        editTaskViewModel.executeFunction(FunctionName.SaveTask(text))
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                } else {
                    editTextManager.reloadText { sentences, indice ->
                        editTextManager.text { text ->
                            editTaskViewModel.executeFunction(FunctionName.SaveTask(text))
                            isEnabled = false
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            }

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
        //reajusto cosas relacionadas a las horaciones si está editando una nota
        reajustSentenceValues()
    }

    private fun reajustSentenceValues() {
        editTextManager.getSentences { sentences, indice ->
            ttsManager.reloadSentences(sentences)
            reajustRangeSliderProgress(sentences.size.toFloat() - 1)
        }
    }

    private fun play(play: Boolean) {
        editTextManager.modifiedVerify {
            if (it == "false") {
                editTextManager.getSentences { sentences, indice ->
                    reajustRangeSliderProgress(sentences.size.toFloat() - 1, indice.toFloat())
                    ttsManager.togglePlayback(sentences, indice, play)
                }
            } else {
                editTextManager.reloadText { sentences, indice ->
                    reajustRangeSliderProgress(sentences.size.toFloat() - 1, indice.toFloat())
                    ttsManager.togglePlayback(sentences, indice, play)
                }
            }
        }
    }

    private fun next(){
        ttsManager.changinParagraphWithControls = true
        val slider = binding.rsTalkProgess
        slider.value = if(slider.value < slider.valueTo) slider.value + 1f else slider.valueTo
    }
    private fun previus(){
        ttsManager.changinParagraphWithControls = true
        val slider = binding.rsTalkProgess
        slider.value = if(slider.value > slider.valueFrom) slider.value - 1f else slider.valueFrom
    }

    private fun reajustRangeSliderProgress(toValue: Float, start:Float = 0f){
         binding.rsTalkProgess.value = if (start > 0f) start else 0f
        if (toValue > 0) binding.rsTalkProgess.valueTo = toValue else null
    }

    private fun addTaskTextFromUrl(url: String?) {
        val urlAux = url ?: arguments?.getString("url")
        if (!urlAux.isNullOrEmpty()) lifecycleScope.launch(Dispatchers.IO) {
            dialogsViewModel.getTextFromUrl(urlAux)
        }
    }

    private fun addOverEditingTask(taskToEdit: String) {
        insertTextIntoEditText(arg.task!!, arg.fontSize)
        arguments?.clear()//TODO MEDIDA TEMPORAL, PUEDE CAUSAR ALGUN ERROR
        lifecycleScope.launch(Dispatchers.IO) {
            editTaskViewModel.executeFunction(
                FunctionName.GetTask(taskToEdit)
            )
        }
    }

    private fun insertTextIntoEditText(text: String, fontSize: Float? = null) {
        if (fontSize != null) editTextManager.fontSize = fontSize
        editTextManager.setText(text.separateSentencesInsertPTag())
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
                        } else {
                            circularProgressBar.visibility = View.GONE
                            if (value.error.isBlank() && !dialogsViewModel.textGotFromUrl.isNullOrEmpty()) {
                                editTextManager.setText(dialogsViewModel.textGotFromUrl!!)
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

    private fun observeIsPlaying() {
        lifecycleScope.launch(Dispatchers.IO) {
            ttsManager._isPlaying.collect { value ->
                if (value.isSpeaking) {
                    updateUiForSpeakingState()
                } else if (value.error.isNotBlank()) showErrorToast() else resetUiState()
                executeNextTaskIfFinalized(value)
            }
        }
    }

    private fun observeTextSize() {
        lifecycleScope.launch(Dispatchers.IO) {
            dialogsViewModel.preferences.collect { preferences ->
                withContext(Dispatchers.Main) {
                    editTextManager.fontSize = preferences.textSize
                }
            }
        }
    }

    private fun activeNotification() {
        try {
            Intent(requireContext().applicationContext, MediaPlayerNotification::class.java).also {
                it.action = "PLAY"
                requireContext().startService(it)
            }
        } catch (e: Exception) {
            Logger.e(e.toString())
        }
    }

    private fun closeNotificationPlayer(pause: Boolean = false) {
        try {
            Intent(requireContext().applicationContext, MediaPlayerNotification::class.java).also {
                it.action = if (pause) "PAUSE" else "STOP"
                requireContext().startService(it)
            }
        } catch (e: Exception) {
            Logger.e(e.toString())
        }
    }

    private suspend fun updateUiForSpeakingState() {
        withContext(Dispatchers.Main) {
            binding.apply {
                editTextManager.editable = false
                btnPlay.visibility = View.INVISIBLE
                btnPause.visibility = View.VISIBLE
                rsTalkProgess.isEnabled = false
            }
        }
    }

    private suspend fun resetUiState() {
        withContext(Dispatchers.Main) {
            binding.apply {
                editTextManager.editable = true
                btnPlay.visibility = View.VISIBLE
                btnPause.visibility = View.INVISIBLE
                rsTalkProgess.isEnabled = true
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

    private fun executeNextTaskIfFinalized(value: SpeakingState) {
        if (value.finalized && !value.isSpeaking) {
            editTaskViewModel.executeFunction(FunctionName.ReadNextTask(editTextManager) {
                play(true)
            })
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
        closeNotificationPlayer()
        super.onDestroy()
    }
}
