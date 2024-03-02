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
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
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
import com.tjm.talkmy.domain.models.FunctionName
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
    private val configsViewModel by viewModels<ConfigsViewModel>()
    private lateinit var tts: TextToSpeech
    private lateinit var ttsManager: TTSManager

    private val arg: EditTaskFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        configsViewModel.getAllPreferences()
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
            configsViewModel.executeFunction(FunctionName.SaveTask(binding.etTask.text.toString()))
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
        configsViewModel.executeFunction(FunctionName.ClickParagraph {
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
            insertTextIntoEditText(arg.task, binding.etTask, binding.tvTextAux, arg.fontSize)
            lifecycleScope.launch(Dispatchers.IO) {
                configsViewModel.executeFunction(
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
        text: String?,
        editText: EditText,
        auxTextView: TextView? = null,
        fontSize: Float? = null
    ) {
        if (auxTextView != null) {
            editText.hint = ""
            auxTextView.visibility = View.VISIBLE
            fontSize?.let { auxTextView.textSize = it }
            auxTextView.text = text?.chunked(1000)?.get(0)
        }
        lifecycleScope.launch(Dispatchers.Main) {
            editText.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        requireActivity().runOnUiThread {
                            editText.viewTreeObserver.removeOnGlobalLayoutListener(this)
                            editText.apply {
                                hint = getString(R.string.editTaskHint_ES)
                                setSelection(0)
                            }
                            auxTextView?.visibility = View.GONE
                            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
                        }
                    }
                }
            )
            withContext(Dispatchers.Default) { editText.setText(text) }
        }
    }

    private fun observeTextFromUrl() {
        lifecycleScope.launch(Dispatchers.IO) {
            dialogsViewModel.getTextFromUrlProcces.collectLatest { value ->
                withContext(Dispatchers.Main) {
                    if (value.isLoading) {
                        binding.apply {
                            circularProgressBar.visibility = View.VISIBLE
                        }
                        binding.etTask.isEnabled = false
                    } else if (value.error.isNotBlank()) {
                        binding.apply {
                            circularProgressBar.visibility = View.GONE
                        }
                        binding.etTask.isEnabled = true
                        Toast.makeText(requireContext(), value.error, Toast.LENGTH_SHORT)
                            .show()
                    } else if (!dialogsViewModel.textGotFromUrl.isNullOrEmpty()) {
                        configsViewModel.executeFunction(FunctionName.SaveTask(binding.etTask.text.toString()))
                        binding.circularProgressBar.visibility = View.GONE
                        binding.etTask.isEnabled = true
                        binding.etTask.setText(dialogsViewModel.textGotFromUrl)
                    }
                }
            }
        }
    }

    private fun observeIsplaying() {
        lifecycleScope.launch(Dispatchers.IO) {
            ttsManager._isPlaying.collect { value ->
                if (value.isSpeaking) {
                    withContext(Dispatchers.Main) {
                        binding.etTask.isFocusableInTouchMode = false
                        binding.apply {
                            btnPlay.visibility = View.INVISIBLE
                            btnPause.visibility = View.VISIBLE
                            rsTalkProgess.isEnabled = false
                        }
                    }
                } else if (value.error.isNotBlank()) {
                    Toast.makeText(
                        requireContext(),
                        "Un error a ocurrido.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    withContext(Dispatchers.Main) {
                        binding.etTask.isFocusableInTouchMode = true
                        binding.apply {
                            btnPlay.visibility = View.VISIBLE
                            btnPause.visibility = View.INVISIBLE
                            rsTalkProgess.isEnabled = true
                        }
                    }
                }
                if (value.finalized && !value.isSpeaking) {
                    configsViewModel.executeFunction(FunctionName.ReadNextTask(binding.etTask) { play() })
                }
            }
        }
    }

    private fun observeTextSize() {
        lifecycleScope.launch(Dispatchers.IO) {
            dialogsViewModel.preferences.collect {
                withContext(Dispatchers.Main) {
                    binding.etTask.textSize = it.textSize
                }
            }
        }
    }

    override fun onInit(status: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            dialogsViewModel.preferences.collectLatest { preferences ->
                if (status == TextToSpeech.SUCCESS) {
                    val output = tts.setLanguage(Locale.getDefault())
                    if (output == TextToSpeech.LANG_MISSING_DATA || output == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.i("yo", "Error con el idioma")
                    } else {
                        ttsManager.configTTS(preferences)
                        withContext(Dispatchers.Main) {
                            binding.apply {
                                btnPlay.isEnabled = true
                                btnPause.isEnabled = true
                            }
                        }
                        observeIsplaying()
                        dialogsViewModel.createSelectVoicesDialog(tts, requireContext())
                    }
                } else {
                    Log.i("yo", "Error con el TTS")
                }
            }
        }
    }

    private fun observeHightlight() {
        lifecycleScope.launch(Dispatchers.IO) {
            ttsManager.currentSentenceToHighlight.collect {
                withContext(Dispatchers.Main) {
                    binding.etTask.apply {
                        setSelection(it.start, it.start + it.sentence.length)
                        requestFocus()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        ttsManager.destroyTTS()
        super.onDestroy()
    }
}
