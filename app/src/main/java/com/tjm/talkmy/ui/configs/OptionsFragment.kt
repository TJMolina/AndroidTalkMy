package com.tjm.talkmy.ui.configs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.tjm.talkmy.databinding.FragmentOptionsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class OptionsFragment : Fragment() {

    private var _binding: FragmentOptionsBinding? = null
    private val binding get() = _binding!!

    private val optionsViewModel by viewModels<OptionsViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initUI()
    }

    private fun initUI() {
        optionsViewModel.getAllPreferences()
        initListeners()
        initCheckers()
    }

    private fun initCheckers() {
        lifecycleScope.launch(Dispatchers.IO) {
            optionsViewModel.preferences.collectLatest { value->
                withContext(Dispatchers.Main) {
                    binding.switchSaveOnline.isChecked = value.saveOnline
                    binding.switchReadNextTask.isChecked = value.readNextTask
                }
            }
        }
    }

    private fun initListeners() {
        binding.switchReadNextTask.setOnClickListener {
            optionsViewModel.saveReadNextTaskOption(binding.switchReadNextTask.isChecked)

        }
        binding.switchSaveOnline.setOnClickListener {
            optionsViewModel.saveTaskOnlineOption(binding.switchSaveOnline.isChecked)
        }
    }

}