package com.example.todolist.data.repository

import com.example.todolist.data.local.dao.TaskDao
import com.example.todolist.data.local.entity.Task
import com.example.todolist.data.local.entity.TaskStatus
import com.example.todolist.reminder.TaskReminderScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository sits between the ViewModel and the DAO.
 * It also owns the lifecycle of the WorkManager-backed reminder:
 *   - upsert / update → (re)schedule
 *   - delete          → cancel
 */
@Singleton
class TaskRepository @Inject constructor(
    private val dao: TaskDao,
    private val reminderScheduler: TaskReminderScheduler
) {
    /* ---------------- Reads ---------------- */
    fun observeAll(): Flow<List<Task>> = dao.observeAll()

    fun observeByDay(dayStartMillis: Long, dayEndMillis: Long): Flow<List<Task>> =
        dao.observeByDay(dayStartMillis, dayEndMillis)

    fun observeByStatus(status: TaskStatus): Flow<List<Task>> =
        dao.observeByStatus(status)

    suspend fun getById(id: Long): Task? = dao.getById(id)

    /* ---------------- Writes ---------------- */
    /**
     * Inserts a new task or replaces an existing one (id != 0).
     * Returns the row id Room assigned. Schedules a reminder for the
     * resulting task using its final id so the Worker can look it up.
     */
    suspend fun upsert(task: Task): Long {
        val newId = dao.upsert(task)
        // Use newId so brand-new tasks (id = 0 going in) get scheduled
        // under the auto-generated row id Room produced.
        val saved = task.copy(id = if (task.id == 0L) newId else task.id)
        reminderScheduler.schedule(saved)
        return newId
    }

    suspend fun update(task: Task) {
        dao.update(task)
        reminderScheduler.schedule(task)         // re-schedule with new times/title
    }

    suspend fun delete(task: Task) {
        reminderScheduler.cancel(task.id)
        dao.delete(task)
    }

    suspend fun deleteById(id: Long) {
        reminderScheduler.cancel(id)
        dao.deleteById(id)
    }
}
