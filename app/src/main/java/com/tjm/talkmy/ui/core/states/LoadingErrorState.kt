package com.tjm.talkmy.ui.core.states

data class LoadingErrorState(
    val isLoading: Boolean = false,
    val error: String = ""
)