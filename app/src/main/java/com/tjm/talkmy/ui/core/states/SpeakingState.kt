package com.tjm.talkmy.ui.core.states

data class SpeakingState(
    val isSpeaking:Boolean = false,
    val finalized:Boolean = false,
    val error:String = ""
)