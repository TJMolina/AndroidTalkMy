package com.tjm.talkmy.domain.models

data class Task(
    val id: String? = null,
    var nota: String,
    val fecha: String? = null,
    val completada: Boolean = false
)