package com.example.todolist.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.todolist.data.local.entity.Task
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskcard.TaskCard
import com.example.taskcard.TaskPriority as UiTaskPriority
import com.example.taskcard.TaskStatus as UiTaskStatus
import com.example.todolist.data.local.entity.Priority as EntityPriority
import com.example.todolist.data.local.entity.TaskStatus as EntityTaskStatus
import com.example.todolist.ui.calendar.HorizontalCalendar
import com.example.todolist.ui.tasks.add.AddTaskSheet
import com.example.todolist.ui.tasks.swipe.SwipeToDeleteTaskCard
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Top-level screen for the app.
 *
 *  ┌───────────────────────────┐
 *  │   Horizontal calendar     │
 *  ├───────────────────────────┤
 *  │   LazyColumn of tasks     │
 *  │   (swipe-to-delete)       │
 *  └───────────────────────────┘
 *  FAB → opens AddTaskSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel = hiltViewModel()
) {
    val tasks        by viewModel.tasks.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    /* ---------- Sheet state ---------- */
    var showSheet    by remember { mutableStateOf(false) }
    var editingTask  by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick        = {
                    editingTask = null            // Add mode
                    showSheet   = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = Color.White,
                shape          = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add task")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            /* ---------- Top horizontal calendar ---------- */
            HorizontalCalendar(viewModel = viewModel)

            /* ---------- Task list for the selected date ---------- */
            if (tasks.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = "No tasks for ${selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items = tasks, key = { it.id }) { task ->
                        SwipeToDeleteTaskCard(
                            task     = task,
                            onDelete = viewModel::deleteTask,
                            modifier = Modifier.animateItem()
                        ) {
                            TaskCard(
                                title     = task.title,
                                category  = task.category.ifBlank { task.description.ifBlank { "—" } },
                                startTime = task.startTime.toDisplayTime(),
                                endTime   = task.endTime.toDisplayTime(),
                                progress  = (task.progress / 100f).coerceIn(0f, 1f),
                                status    = task.status.toUi(),
                                priority  = task.priority.toUi(),
                                avatars   = emptyList(),
                                onEdit    = {
                                    editingTask = task
                                    showSheet   = true
                                },
                                onDelete  = { viewModel.deleteTask(task) },
                                onToggleComplete = {
                                    val next = if (task.status == EntityTaskStatus.COMPLETED)
                                        EntityTaskStatus.PENDING
                                    else
                                        EntityTaskStatus.COMPLETED
                                    viewModel.setStatus(task, next)
                                },
                                modifier  = Modifier.clickable {
                                    editingTask = task
                                    showSheet   = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    /* ---------- ModalBottomSheet for adding OR editing a task ---------- */
    if (showSheet) {
        AddTaskSheet(
            onDismiss = {
                showSheet   = false
                editingTask = null
            },
            viewModel  = viewModel,
            taskToEdit = editingTask          // null → Add mode, non-null → Edit mode
        )
    }
}

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("hh a", Locale.getDefault())

private fun Long.toDisplayTime(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(timeFormatter)
        .lowercase(Locale.getDefault())

private fun EntityTaskStatus.toUi(): UiTaskStatus = when (this) {
    EntityTaskStatus.PENDING   -> UiTaskStatus.PENDING
    EntityTaskStatus.RUNNING   -> UiTaskStatus.RUNNING
    EntityTaskStatus.COMPLETED -> UiTaskStatus.COMPLETED
    EntityTaskStatus.CANCELLED -> UiTaskStatus.CANCELLED
}

private fun EntityPriority.toUi(): UiTaskPriority = when (this) {
    EntityPriority.LOW    -> UiTaskPriority.LOW
    EntityPriority.MEDIUM -> UiTaskPriority.MEDIUM
    EntityPriority.HIGH   -> UiTaskPriority.HIGH
}
