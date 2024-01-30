package com.tjm.talkmy.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tjm.talkmy.domain.models.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

@Entity(tableName = "tasks_table")
data class TaskEntitiy(
    @PrimaryKey
    @ColumnInfo(name = "id") val id:String,
    @ColumnInfo(name = "nota") val nota: String,
    @ColumnInfo(name = "fecha") val fecha: String = SimpleDateFormat("yyyy-MM-dd").format(Date()),
    @ColumnInfo(name = "completada") val completada: Boolean = false
) {
    fun toDomain() = Task(id = id, nota = nota, fecha = fecha, completada = completada)
}

fun Task.toDatabase() =
    TaskEntitiy(id = this.id ?: UUID.randomUUID().toString(), nota = this.nota, completada = this.completada)

