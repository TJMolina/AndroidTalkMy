package com.tjm.talkmy.ui.configs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.orhanobut.logger.Logger
import com.tjm.talkmy.R
import com.tjm.talkmy.databinding.FragmentOptionsBinding
import com.tjm.talkmy.domain.models.PreferencesType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
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
        initConfigs()
        initSpinner()
    }

    private fun initSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.options_ordernotes,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerOrderNotes.adapter = adapter
        }
    }

    private fun initConfigs() {
        lifecycleScope.launch(Dispatchers.IO) {
            val preferences = optionsViewModel.getAllPreferences()
            withContext(Dispatchers.Main) {
                binding.switchSaveOnline.isChecked = preferences.saveOnline
                binding.switchReadNextTask.isChecked = preferences.readNextTask
                binding.switchReadParagraph.isChecked = preferences.clickParagraph
                binding.spinnerOrderNotes.setSelection(if (preferences.orderNote) 0 else 1)
                initListeners()
            }
        }
    }

    private fun initListeners() {
        binding.switchReadNextTask.setOnCheckedChangeListener { buttonView, isChecked ->
            optionsViewModel.savePreference(
                isChecked,
                PreferencesType.NEXTTASK
            )
        }
        binding.switchSaveOnline.setOnCheckedChangeListener { buttonView, isChecked ->
            optionsViewModel.savePreference(
                isChecked,
                PreferencesType.SAVEONLINE
            )
        }
        binding.switchReadParagraph.setOnCheckedChangeListener { buttonView, isChecked ->
            optionsViewModel.savePreference(
                isChecked,
                PreferencesType.CLICKPARAGRAPH
            )
        }
        binding.switchDarkModeOn.setOnCheckedChangeListener { buttonView, isChecked ->
            optionsViewModel.savePreference(
                isChecked,
                PreferencesType.DARKMODEON
            )
        }
        binding.spinnerOrderNotes.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    optionsViewModel.savePreference(position == 0, PreferencesType.ORDERNOTE)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

}