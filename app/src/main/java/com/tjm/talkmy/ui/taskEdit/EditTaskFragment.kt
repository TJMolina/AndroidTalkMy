package com.tjm.talkmy.ui.taskEdit

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.addCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.tjm.talkmy.R
import com.tjm.talkmy.databinding.FragmentEditTaskBinding
import com.tjm.talkmy.ui.core.TTSManager
import com.tjm.talkmy.ui.core.extensions.isURL
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
        initTTS()
        initMenu()
        getTask()
        initEvents()
        initListeners()
    }

    private fun initMenu() {
        val menuHost: MenuHost = binding.topAppBar
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.ivSave -> {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                        true
                    }

                    R.id.btnPageUrl -> {
                        showDialog()
                        true
                    }

                    else -> false
                }
            }

        })
    }

    private fun initTTS() {
        tts = TextToSpeech(requireContext(), this)
        ttsManager = TTSManager(tts, requireActivity())
    }

    private fun getTask() {
        val url = arguments?.getString("url")
        if (!url.isNullOrEmpty()) getTextFromUrl(url)
        else {
            editTaskViewModel.getTask(arg.taskToEdit, binding.etTask)
        }
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

    @SuppressLint("ClickableViewAccessibility")
    private fun initListeners() {
        binding.btnPlay.apply {
            isEnabled = false
            setOnClickListener { play() }
        }
        binding.btnPause.apply {
            isEnabled = false
            setOnClickListener { play() }
        }
        binding.etTask.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val position = binding.etTask.getOffsetForPosition(event.x, event.y)
                editTaskViewModel.getPositionClicked(view as EditText, position, ttsManager)
            }
            false
        }
    }

    private fun play() {
        ttsManager.togglePlayback(binding.etTask.text.toString(), binding.etTask)
    }

    fun recivedUrl(url:String?){
        if (!url.isNullOrEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                editTaskViewModel.saveTask(binding.etTask)
                getTextFromUrl(url)
            }
        }
    }

     private fun getTextFromUrl(url: String) {
        editTaskViewModel.getTextFromUrl(url)
        lifecycleScope.launch(Dispatchers.IO) {
            editTaskViewModel.getTextFromUrlProcces.collectLatest { value ->
                withContext(Dispatchers.Main) {
                    if (value.isLoading) {
                        binding.circularProgressBar.visibility = View.VISIBLE
                        binding.etTask.isEnabled = false
                    } else if (value.error.isNotBlank()) {
                    } else {
                        binding.circularProgressBar.visibility = View.GONE
                        binding.etTask.setText(editTaskViewModel.textGotFromUrl)
                        binding.etTask.isEnabled = true
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
                        binding.etTask.isEnabled = false
                        binding.btnPlay.visibility = View.INVISIBLE
                        binding.btnPause.visibility = View.VISIBLE
                    }
                } else if (value.error.isNotBlank()) {

                } else {
                    withContext(Dispatchers.Main) {
                        binding.etTask.isEnabled = true
                        binding.btnPlay.visibility = View.VISIBLE
                        binding.btnPause.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private fun showDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_insert_url)
        val btnGetTextFromUrl = dialog.findViewById<Button>(R.id.btnGetTextFromUrl)
        val edUrl = dialog.findViewById<EditText>(R.id.edUrl)
        val btnCloceDialog = dialog.findViewById<ImageButton>(R.id.btnCloceDialog)

        btnCloceDialog.setOnClickListener {
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
            val output = tts.setLanguage(Locale.getDefault())
            if (output == TextToSpeech.LANG_MISSING_DATA || output == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.i("yo", "error con lo del idioma")
            } else {
                binding.btnPlay.isEnabled = true
                binding.btnPause.isEnabled = true
                observeIsplaying()
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
