package com.tjm.talkmy.domain.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

data class Task(
    var id: String = UUID.randomUUID().toString(),
    var nota: String,
    val fecha: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
    val completada: Boolean = false
)