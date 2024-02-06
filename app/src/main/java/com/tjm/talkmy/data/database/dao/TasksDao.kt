package com.tjm.talkmy.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tjm.talkmy.data.database.entities.TaskEntitiy

@Dao
interface TasksDao {
    @Query("SELECT * FROM tasks_table ORDER BY  fecha ASC")
    suspend fun getAllTasks():List<TaskEntitiy>

    @Query("SELECT * FROM tasks_table WHERE id = :idTask")
    suspend fun getTask(idTask: String): TaskEntitiy

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task:TaskEntitiy)

    @Query("DELETE FROM tasks_table")
    suspend fun deleteAllTasks()

    @Query("DELETE FROM tasks_table WHERE id = :idTask")
    suspend fun deleteTask(idTask: String)

}