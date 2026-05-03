package com.example.todolist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.todolist.data.local.entity.Task
import com.example.todolist.data.local.entity.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    /* ----------  WRITES  ---------- */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: Long)

    /* ----------  READS  ---------- */

    @Query("SELECT * FROM tasks ORDER BY startTime ASC")
    fun observeAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getById(taskId: Long): Task?

    /**
     * All tasks whose [startTime] falls within the day represented by
     * [dayStartMillis] (inclusive) and [dayEndMillis] (exclusive).
     * The ViewModel computes those bounds from a LocalDate.
     */
    @Query("""
        SELECT * FROM tasks
        WHERE startTime >= :dayStartMillis AND startTime < :dayEndMillis
        ORDER BY startTime ASC
    """)
    fun observeByDay(dayStartMillis: Long, dayEndMillis: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY startTime ASC")
    fun observeByStatus(status: TaskStatus): Flow<List<Task>>
}
