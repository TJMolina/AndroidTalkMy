package com.tjm.talkmy.ui.taskEdit

import android.widget.EditText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tjm.talkmy.core.ResponseState
import com.tjm.talkmy.domain.models.Task
import com.tjm.talkmy.domain.useCases.getTaskUseCase
import com.tjm.talkmy.domain.useCases.getTasksUseCase
import com.tjm.talkmy.domain.useCases.onlineUseCases.GetTextFromUrlUseCase
import com.tjm.talkmy.domain.useCases.uploadTaskUseCasea
import com.tjm.talkmy.ui.taskEdit.managers.TTSManager
import com.tjm.talkmy.ui.core.states.LoadingErrorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EditTaskViewModel @Inject constructor(
) :
    ViewModel() {
}