package com.example.todolist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the lifecycle state of a task.
 * Stored in the DB as the enum's string name via a TypeConverter.
 */
enum class TaskStatus { PENDING, RUNNING, COMPLETED, CANCELLED }

enum class Priority { LOW, MEDIUM, HIGH }

/**
 * Room entity for a single to-do item.
 *
 * `startTime` / `endTime` are stored as epoch milliseconds (UTC) for easy
 * range-querying and trivial sorting. Convert to LocalDateTime in the UI layer.
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val title: String,
    val description: String = "",

    /** Epoch millis (UTC). */
    val startTime: Long,
    /** Epoch millis (UTC). */
    val endTime: Long,

    /** 0..100 percent. */
    val progress: Int = 0,

    val status: TaskStatus = TaskStatus.PENDING,

    val priority: Priority = Priority.MEDIUM,

    val category: String = "",

    val createdAt: Long = System.currentTimeMillis()
)
