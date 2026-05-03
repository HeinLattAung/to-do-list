package com.example.todolist.ui.tasks.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todolist.data.local.entity.Task
import com.example.todolist.data.local.entity.TaskStatus
import com.example.todolist.ui.tasks.TaskViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/* =============================================================
 *  Pastel palette — same colours used on TaskCard, kept in one
 *  place so this sheet stays visually consistent with the list.
 * ============================================================= */
private data class StatusPalette(val bg: Color, val accent: Color, val label: String)

private fun TaskStatus.palette(): StatusPalette = when (this) {
    TaskStatus.PENDING   -> StatusPalette(Color(0xFFE6DCF7), Color(0xFF7B5BD6), "Pending")
    TaskStatus.RUNNING   -> StatusPalette(Color(0xFFD8E6FB), Color(0xFF1A73E8), "Running")
    TaskStatus.COMPLETED -> StatusPalette(Color(0xFFD9F2E3), Color(0xFF34A853), "Completed")
    TaskStatus.CANCELLED -> StatusPalette(Color(0xFFFFE0E0), Color(0xFFD93025), "Cancelled")
}

/* =============================================================
 *  Public composable
 *  -----------------------------------------------------------
 *  Pass `taskToEdit = null` (or omit it) for a brand-new task.
 *  Pass an existing Task to enter Edit mode — fields come pre-
 *  filled and Save calls viewModel.updateTask() instead of add.
 * ============================================================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskSheet(
    onDismiss: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel(),
    taskToEdit: Task? = null
) {
    val sheetState   = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope        = rememberCoroutineScope()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    val isEditMode = taskToEdit != null

    /* ---------- Derive initial values once per opening ---------- */
    val initial = remember(taskToEdit) {
        InitialFormValues.from(taskToEdit)
    }

    /* ---------- Form state, seeded from taskToEdit when present ---------- */
    var title       by remember(taskToEdit) { mutableStateOf(initial.title) }
    var category    by remember(taskToEdit) { mutableStateOf(initial.category) }
    var description by remember(taskToEdit) { mutableStateOf(initial.description) }
    var status      by remember(taskToEdit) { mutableStateOf(initial.uiStatus) }

    /* ---------- Time picker state, seeded from taskToEdit ---------- */
    val startState = rememberTimePickerState(
        initialHour   = initial.startHour,
        initialMinute = initial.startMinute,
        is24Hour      = false
    )
    val endState = rememberTimePickerState(
        initialHour   = initial.endHour,
        initialMinute = initial.endMinute,
        is24Hour      = false
    )
    var showStart by remember { mutableStateOf(false) }
    var showEnd   by remember { mutableStateOf(false) }

    /* ---------- Validation ---------- */
    val canSave = title.isNotBlank() &&
            (endState.totalMinutes() > startState.totalMinutes())

    val accent = status.palette().accent

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        shape            = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor   = Color.White,
        dragHandle       = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {

            /* ---------- Header ---------- */
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
            ) {
                Text(
                    text       = if (isEditMode) "Edit Task" else "New Task",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            /* ---------- Title ---------- */
            FieldLabel("Title")
            OutlinedTextField(
                value          = title,
                onValueChange  = { title = it },
                placeholder    = { Text("e.g. UI/UX Wireframes") },
                singleLine     = true,
                shape          = RoundedCornerShape(16.dp),
                colors         = pastelFieldColors(accent),
                modifier       = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            /* ---------- Category ---------- */
            FieldLabel("Category")
            OutlinedTextField(
                value          = category,
                onValueChange  = { category = it },
                placeholder    = { Text("e.g. Design Sprint") },
                singleLine     = true,
                shape          = RoundedCornerShape(16.dp),
                colors         = pastelFieldColors(accent),
                modifier       = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            /* ---------- Description (optional) ---------- */
            FieldLabel("Description")
            OutlinedTextField(
                value          = description,
                onValueChange  = { description = it },
                placeholder    = { Text("Add notes (optional)") },
                shape          = RoundedCornerShape(16.dp),
                colors         = pastelFieldColors(accent),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                minLines       = 2,
                maxLines        = 4,
                modifier       = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            /* ---------- Time row ---------- */
            FieldLabel("Time")
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                TimeChip(
                    label     = "Start",
                    text      = startState.formatLabel(),
                    accent    = accent,
                    onClick   = { showStart = true },
                    modifier  = Modifier.weight(1f)
                )
                TimeChip(
                    label     = "End",
                    text      = endState.formatLabel(),
                    accent    = accent,
                    onClick   = { showEnd = true },
                    modifier  = Modifier.weight(1f)
                )
            }
            if (!canSave && title.isNotBlank()) {
                Text(
                    text     = "End time must be after start time",
                    color    = Color(0xFFD93025),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(Modifier.height(18.dp))

            /* ---------- Status chips ---------- */
            FieldLabel("Status")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding        = PaddingValues(vertical = 4.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                items(items = TaskStatus.values()) { s ->
                    StatusChip(
                        status      = s,
                        isSelected  = s == status,
                        onClick     = { status = s }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            /* ---------- Save button ---------- */
            Button(
                onClick = {
                    val task = buildTask(
                        existing    = taskToEdit,
                        title       = title.trim(),
                        category    = category.trim(),
                        description = description.trim(),
                        status      = status,
                        date        = selectedDate,
                        startTime   = startState.toLocalTime(),
                        endTime     = endState.toLocalTime()
                    )
                    if (taskToEdit == null) viewModel.addTask(task)
                    else                    viewModel.updateTask(task)

                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) onDismiss()
                    }
                },
                enabled  = canSave,
                shape    = RoundedCornerShape(20.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor   = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text       = if (isEditMode) "Update Task" else "Save Task",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    /* ---------- Time picker dialogs ---------- */
    if (showStart) {
        TimePickerDialog(
            title        = "Select start time",
            confirmColor = accent,
            onDismiss    = { showStart = false },
            onConfirm    = { showStart = false }
        ) { TimePicker(state = startState) }
    }
    if (showEnd) {
        TimePickerDialog(
            title        = "Select end time",
            confirmColor = accent,
            onDismiss    = { showEnd = false },
            onConfirm    = { showEnd = false }
        ) { TimePicker(state = endState) }
    }
}

/* =============================================================
 *  Sub-components
 * ============================================================= */

@Composable
private fun FieldLabel(text: String) {
    Text(
        text       = text,
        fontSize   = 13.sp,
        fontWeight = FontWeight.Medium,
        color      = Color(0xFF555555),
        modifier   = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun TimeChip(
    label: String,
    text: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick     = onClick,
        shape       = RoundedCornerShape(16.dp),
        color       = Color(0xFFF5F5F7),
        modifier    = modifier
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector       = Icons.Default.Schedule,
                contentDescription = null,
                tint              = accent,
                modifier          = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(label, fontSize = 11.sp, color = Color.Gray)
                Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StatusChip(
    status: TaskStatus,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val palette = status.palette()
    val bg      = if (isSelected) palette.accent else palette.bg
    val fg      = if (isSelected) Color.White else palette.accent

    Surface(
        onClick = onClick,
        shape   = RoundedCornerShape(20.dp),
        color   = bg,
        modifier = Modifier.height(40.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 18.dp)
        ) {
            Text(
                text       = palette.label,
                color      = fg,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/* =============================================================
 *  Time-picker dialog wrapper — Material 3 doesn't ship one.
 * ============================================================= */
@Composable
private fun TimePickerDialog(
    title: String,
    confirmColor: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape    = RoundedCornerShape(28.dp),
            color    = Color.White,
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text       = title,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.padding(bottom = 16.dp)
                )
                content()
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
                    TextButton(onClick = onConfirm) { Text("OK", color = confirmColor, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

/* =============================================================
 *  Helpers
 * ============================================================= */

@OptIn(ExperimentalMaterial3Api::class)
private fun TimePickerState.totalMinutes(): Int = hour * 60 + minute

@OptIn(ExperimentalMaterial3Api::class)
private fun TimePickerState.toLocalTime(): LocalTime = LocalTime.of(hour, minute)

@OptIn(ExperimentalMaterial3Api::class)
private fun TimePickerState.formatLabel(): String {
    val lt = toLocalTime()
    val h12 = ((lt.hour + 11) % 12) + 1
    val ampm = if (lt.hour < 12) "AM" else "PM"
    return "%02d:%02d %s".format(h12, lt.minute, ampm)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun pastelFieldColors(accent: Color) =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor   = accent,
        unfocusedBorderColor = Color(0xFFE0E0E0),
        cursorColor          = accent,
        focusedContainerColor   = Color(0xFFFAFAFA),
        unfocusedContainerColor = Color(0xFFFAFAFA)
    )

/**
 * Build a Task from form fields.
 *  - In Add mode  (existing == null) → returns a fresh Task with id = 0 (Room
 *    will auto-generate one) and a default initial progress.
 *  - In Edit mode (existing != null) → preserves id, createdAt and progress
 *    so the row is updated in place rather than duplicated.
 */
private fun buildTask(
    existing: Task?,
    title: String,
    category: String,
    description: String,
    status: TaskStatus,
    date: LocalDate,
    startTime: LocalTime,
    endTime: LocalTime
): Task {
    val zone        = ZoneId.systemDefault()
    val startMillis = date.atTime(startTime).atZone(zone).toInstant().toEpochMilli()
    val endMillis   = date.atTime(endTime).atZone(zone).toInstant().toEpochMilli()

    val initialProgress = when (status) {
        TaskStatus.COMPLETED -> 100
        TaskStatus.RUNNING   -> 25
        else                 -> 0
    }

    return if (existing == null) {
        Task(
            title       = title,
            description = description,
            category    = category,
            startTime   = startMillis,
            endTime     = endMillis,
            progress    = initialProgress,
            status      = status
        )
    } else {
        // Preserve id + createdAt; auto-update progress only when status changed
        val newProgress = if (status != existing.status) initialProgress else existing.progress
        existing.copy(
            title       = title,
            description = description,
            category    = category,
            startTime   = startMillis,
            endTime     = endMillis,
            status      = status,
            progress    = newProgress
        )
    }
}

/* =============================================================
 *  Holds the initial values used to seed form state when the sheet
 *  opens. Calculated once via remember(taskToEdit).
 * ============================================================= */
private data class InitialFormValues(
    val title: String,
    val category: String,
    val description: String,
    val uiStatus: TaskStatus,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int
) {
    companion object {
        fun from(task: Task?): InitialFormValues {
            if (task == null) {
                return InitialFormValues(
                    title       = "",
                    category    = "",
                    description = "",
                    uiStatus    = TaskStatus.PENDING,
                    startHour   = 9,
                    startMinute = 0,
                    endHour     = 10,
                    endMinute   = 0
                )
            }
            val zone  = ZoneId.systemDefault()
            val start = java.time.Instant.ofEpochMilli(task.startTime).atZone(zone).toLocalTime()
            val end   = java.time.Instant.ofEpochMilli(task.endTime).atZone(zone).toLocalTime()
            return InitialFormValues(
                title       = task.title,
                category    = task.category,
                description = task.description,
                uiStatus    = task.status,
                startHour   = start.hour,
                startMinute = start.minute,
                endHour     = end.hour,
                endMinute   = end.minute
            )
        }
    }
}
