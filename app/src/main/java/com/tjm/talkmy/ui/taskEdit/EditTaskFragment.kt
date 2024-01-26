package com.tjm.talkmy.ui.taskEdit

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
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.tjm.talkmy.databinding.FragmentEditTaskBinding
import com.tjm.talkmy.ui.core.extensions.separateSentences
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class EditTaskFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentEditTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var tts: TextToSpeech

    private var currentSentenceIndex = 0
    private var sentences: List<String>? = null

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
        tts = TextToSpeech(requireContext(), this)
        initListeners()
    }

    private fun initListeners() {
        binding.ivSave.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.btnPlay.apply {
            isEnabled = false
            setOnClickListener { play() }
        }
    }

    private fun play() {
        sentences = binding.etTask.text.toString().separateSentences()
        speak()
    }

    // Resaltar la oración actual
    private fun highlightSentence(currentSentence: String) {
        val spannableString = SpannableString(binding.etTask.text.toString())
        val start = binding.etTask.text.toString().indexOf(currentSentence)
        val end = start + currentSentence.length
        spannableString.setSpan(
            BackgroundColorSpan(Color.YELLOW),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.etTask.setText(spannableString)
    }

    private fun speak() {
        if (!sentences.isNullOrEmpty() && currentSentenceIndex < sentences!!.size) {
            val currentSentence = sentences!![currentSentenceIndex]
            tts?.apply {
                speak(currentSentence, TextToSpeech.QUEUE_FLUSH, null, currentSentence)
                setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        highlightSentence(currentSentence)
                    }
                    override fun onDone(utteranceId: String?) {
                        currentSentenceIndex++
                        speak()
                    }
                    override fun onError(utteranceId: String?) {
                        Log.e("tts error:", "algun error al tratar de leer.")
                    }
                })
            }
        } else {
            currentSentenceIndex = 0
        }
    }

    //funcion parte de TextToSpeech
    override fun onInit(status: Int) {
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
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
