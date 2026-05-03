package com.example.todolist.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.local.entity.Task
import com.example.todolist.data.local.entity.TaskStatus
import com.example.todolist.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * UI state holder for the task list screen.
 * The screen observes [tasks] and [selectedDate] via collectAsState().
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    /* -----------------------------------------------------------
     *  Selected-date state — single source of truth for filtering
     * ----------------------------------------------------------- */
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    /* -----------------------------------------------------------
     *  Tasks for the selected date — re-queried whenever the date
     *  changes thanks to flatMapLatest.
     * ----------------------------------------------------------- */
    val tasks: StateFlow<List<Task>> = _selectedDate
        .flatMapLatest { date ->
            val (start, end) = date.dayBoundsMillis()
            repository.observeByDay(start, end)
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /* -----------------------------------------------------------
     *  Public actions
     * ----------------------------------------------------------- */
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addTask(task: Task) = viewModelScope.launch {
        repository.upsert(task)
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        repository.update(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.delete(task)
    }

    fun setStatus(task: Task, status: TaskStatus) = viewModelScope.launch {
        repository.update(task.copy(status = status))
    }

    fun setProgress(task: Task, progress: Int) = viewModelScope.launch {
        val clamped = progress.coerceIn(0, 100)
        val newStatus = when {
            clamped == 100 -> TaskStatus.COMPLETED
            clamped > 0    -> TaskStatus.RUNNING
            else           -> task.status
        }
        repository.update(task.copy(progress = clamped, status = newStatus))
    }
}

/* ---------------------------------------------------------------
 *  Helpers
 * --------------------------------------------------------------- */

/**
 * Returns [start, end) epoch-millis bounds for the given LocalDate
 * in the device's default time zone — exactly what the DAO expects.
 */
private fun LocalDate.dayBoundsMillis(zone: ZoneId = ZoneId.systemDefault()): Pair<Long, Long> {
    val start = this.atStartOfDay(zone).toInstant().toEpochMilli()
    val end   = this.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    return start to end
}
