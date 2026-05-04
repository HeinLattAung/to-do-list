package com.example.todolist.data.local

import androidx.room.TypeConverter
import com.example.todolist.data.local.entity.Priority
import com.example.todolist.data.local.entity.TaskStatus

/**
 * Room TypeConverters. Add more here if you introduce non-primitive fields
 * (e.g. List<String> tags, LocalDateTime, etc.).
 */
class Converters {

    @TypeConverter
    fun fromStatus(status: TaskStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): TaskStatus =
        runCatching { TaskStatus.valueOf(value) }.getOrDefault(TaskStatus.PENDING)

    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority =
        runCatching { Priority.valueOf(value) }.getOrDefault(Priority.MEDIUM)
}
