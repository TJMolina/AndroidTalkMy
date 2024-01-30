package com.tjm.talkmy.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tjm.talkmy.data.database.dao.TasksDao
import com.tjm.talkmy.data.database.entities.TaskEntitiy

@Database(entities = [TaskEntitiy::class], version = 1)
abstract class TaskDataBase : RoomDatabase(){
    abstract fun getTaskDao():TasksDao
}