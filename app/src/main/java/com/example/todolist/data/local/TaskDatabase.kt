package com.example.todolist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.todolist.data.local.dao.TaskDao
import com.example.todolist.data.local.entity.Task

@Database(
    entities  = [Task::class],
    version   = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        const val DATABASE_NAME = "todo_database.db"
    }
}
