package com.tjm.talkmy.domain.models

data class AllPreferences(
    val textSize:Float = 20.0f,
    val volume:Int = 10,
    val speech: Float = 1f,
    val velocity: Float = 1f,
    val voice:String = "",
    val readNextTask:Boolean = false,
    val saveOnline:Boolean = false,
    val clickParagraph:Boolean = true
)