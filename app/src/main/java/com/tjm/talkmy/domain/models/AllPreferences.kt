package com.tjm.talkmy.domain.models

data class AllPreferences(
    val textSize:Float = 20.0f,
    val volume:Int = 10,
    val speech: Float = 0.5f,
    val velocity: Float = 1.0f,
    val voice:String = ""
)