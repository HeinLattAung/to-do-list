package com.example.todolist.ui.tasks.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todolist.data.local.entity.Priority
import com.example.todolist.data.local.entity.Task
import com.example.todolist.data.local.entity.TaskStatus
import com.example.todolist.ui.tasks.TaskViewModel
import com.example.todolist.ui.theme.BgChip
import com.example.todolist.ui.theme.BgElevated
import com.example.todolist.ui.theme.BgInput
import com.example.todolist.ui.theme.BgInputFocused
import com.example.todolist.ui.theme.BorderFocused
import com.example.todolist.ui.theme.BorderSubtle
import com.example.todolist.ui.theme.Coral
import com.example.todolist.ui.theme.Cyan
import com.example.todolist.ui.theme.Lavender
import com.example.todolist.ui.theme.MintGreen
import com.example.todolist.ui.theme.OnCyan
import com.example.todolist.ui.theme.OnLavender
import com.example.todolist.ui.theme.OnMint
import com.example.todolist.ui.theme.TextPrimary
import com.example.todolist.ui.theme.TextSecondary
import com.example.todolist.ui.theme.TextTertiary
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/* =============================================================
 *  Status → accent + label mapping
 * ============================================================= */
private fun TaskStatus.accent(): Color = when (this) {
    TaskStatus.COMPLETED -> MintGreen
    TaskStatus.RUNNING   -> Lavender
    TaskStatus.PENDING   -> Cyan
    TaskStatus.CANCELLED -> Coral
}
private fun TaskStatus.onAccent(): Color = when (this) {
    TaskStatus.COMPLETED -> OnMint
    TaskStatus.RUNNING   -> OnLavender
    TaskStatus.PENDING   -> OnCyan
    TaskStatus.CANCELLED -> Color.White
}
private fun TaskStatus.label(): String = when (this) {
    TaskStatus.COMPLETED -> "Completed"
    TaskStatus.RUNNING   -> "Running"
    TaskStatus.PENDING   -> "Pending"
    TaskStatus.CANCELLED -> "Rejected"
}

/* =============================================================
 *  Public composable
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
    val initial    = remember(taskToEdit, selectedDate) {
        InitialFormValues.from(taskToEdit, selectedDate)
    }

    var title       by remember(taskToEdit) { mutableStateOf(initial.title) }
    var category    by remember(taskToEdit) { mutableStateOf(initial.category) }
    var description by remember(taskToEdit) { mutableStateOf(initial.description) }
    var status      by remember(taskToEdit) { mutableStateOf(initial.status) }
    var priority    by remember(taskToEdit) { mutableStateOf(initial.priority) }

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

    val isToday      = selectedDate == LocalDate.now()
    val nowMinutes   = LocalTime.now().run { hour * 60 + minute }
    val startInPast  = isToday && startState.totalMinutes() < nowMinutes
    val canSave      = title.isNotBlank() &&
            (endState.totalMinutes() > startState.totalMinutes()) &&
            !startInPast

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        shape            = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor   = BgElevated,
        contentColor     = TextPrimary,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TextTertiary.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {

            /* ---------- Header ---------- */
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 20.dp)
            ) {
                Text(
                    text       = if (isEditMode) "Edit Task" else "Create New Task",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary,
                    modifier   = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            /* ---------- Title ---------- */
            FieldLabel("Task Title")
            DarkTextField(
                value         = title,
                onValueChange = { title = it },
                placeholder   = "e.g. UI/UX Wireframes",
                singleLine    = true
            )

            Spacer(Modifier.height(16.dp))

            /* ---------- Category ---------- */
            FieldLabel("Category")
            DarkTextField(
                value         = category,
                onValueChange = { category = it },
                placeholder   = "e.g. Design Sprint",
                singleLine    = true
            )

            Spacer(Modifier.height(16.dp))

            /* ---------- Description ---------- */
            FieldLabel("Description")
            DarkTextField(
                value         = description,
                onValueChange = { description = it },
                placeholder   = "Add notes (optional)",
                singleLine    = false,
                minLines      = 2,
                maxLines      = 4
            )

            Spacer(Modifier.height(20.dp))

            /* ---------- Time row ---------- */
            FieldLabel("Time")
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                TimeChip(
                    label    = "Start",
                    text     = startState.formatLabel(),
                    onClick  = { showStart = true },
                    accent   = status.accent(),
                    modifier = Modifier.weight(1f)
                )
                TimeChip(
                    label    = "End",
                    text     = endState.formatLabel(),
                    onClick  = { showEnd = true },
                    accent   = status.accent(),
                    modifier = Modifier.weight(1f)
                )
            }
            if (title.isNotBlank()) {
                val errorMsg = when {
                    startInPast                                                   ->
                        "Start time can't be earlier than now for today"
                    endState.totalMinutes() <= startState.totalMinutes()          ->
                        "End time must be after start time"
                    else -> null
                }
                if (errorMsg != null) {
                    Text(
                        text     = errorMsg,
                        color    = Coral,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            /* ---------- Status chips ---------- */
            FieldLabel("Status")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding        = PaddingValues(vertical = 4.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                items(items = TaskStatus.values()) { s ->
                    StatusChip(
                        status     = s,
                        isSelected = s == status,
                        onClick    = { status = s }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            /* ---------- Priority chips ---------- */
            FieldLabel("Priority")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding        = PaddingValues(vertical = 4.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                items(items = Priority.values()) { p ->
                    PriorityChipChoice(
                        priority   = p,
                        isSelected = p == priority,
                        onClick    = { priority = p }
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            /* ---------- Save button ---------- */
            Button(
                onClick = {
                    val task = buildTask(
                        existing    = taskToEdit,
                        title       = title.trim(),
                        category    = category.trim(),
                        description = description.trim(),
                        status      = status,
                        priority    = priority,
                        date        = selectedDate,
                        startTime   = startState.toLocalTime(),
                        endTime     = endState.toLocalTime()
                    )
                    if (taskToEdit == null) viewModel.addTask(task) else viewModel.updateTask(task)

                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) onDismiss()
                    }
                },
                enabled  = canSave,
                shape    = RoundedCornerShape(18.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = MintGreen,
                    contentColor           = OnMint,
                    disabledContainerColor = MintGreen.copy(alpha = 0.35f),
                    disabledContentColor   = OnMint.copy(alpha = 0.7f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text       = if (isEditMode) "Update Task" else "Create Task",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    /* ---------- Time picker dialogs ---------- */
    if (showStart) {
        TimePickerDialog(
            title        = "Select start time",
            confirmColor = status.accent(),
            onDismiss    = { showStart = false },
            onConfirm    = { showStart = false }
        ) { TimePicker(state = startState) }
    }
    if (showEnd) {
        TimePickerDialog(
            title        = "Select end time",
            confirmColor = status.accent(),
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
        color      = TextSecondary,
        modifier   = Modifier.padding(bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 4
) {
    OutlinedTextField(
        value           = value,
        onValueChange   = onValueChange,
        placeholder     = { Text(placeholder, color = TextTertiary, fontSize = 14.sp) },
        singleLine      = singleLine,
        minLines        = minLines,
        maxLines        = maxLines,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        textStyle       = TextStyle(
            color      = TextPrimary,
            fontSize   = 15.sp,
            fontWeight = FontWeight.Medium
        ),
        shape           = RoundedCornerShape(14.dp),
        colors          = OutlinedTextFieldDefaults.colors(
            focusedTextColor          = TextPrimary,
            unfocusedTextColor        = TextPrimary,
            focusedContainerColor     = BgInputFocused,
            unfocusedContainerColor   = BgInput,
            disabledContainerColor    = BgInput,
            cursorColor               = MintGreen,
            focusedBorderColor        = BorderFocused,
            unfocusedBorderColor      = BorderSubtle,
            disabledBorderColor       = BorderSubtle,
            focusedPlaceholderColor   = TextTertiary,
            unfocusedPlaceholderColor = TextTertiary
        ),
        modifier = Modifier.fillMaxWidth()
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
        onClick  = onClick,
        shape    = RoundedCornerShape(14.dp),
        color    = BgInput,
        modifier = modifier.border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
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
                Text(label, fontSize = 11.sp, color = TextTertiary)
                Text(text,  fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
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
    val accent = status.accent()
    val bg     = if (isSelected) accent else BgChip
    val fg     = if (isSelected) status.onAccent() else accent
    val border = if (isSelected) accent else BorderSubtle

    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(20.dp),
        color    = bg,
        border   = BorderStroke(1.dp, border),
        modifier = Modifier.height(40.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 18.dp)
        ) {
            Text(
                text       = status.label(),
                color      = fg,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

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
            color    = BgElevated,
            modifier = Modifier.padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text       = title,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                    modifier   = Modifier.padding(bottom = 16.dp)
                )
                content()
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                    TextButton(onClick = onConfirm) {
                        Text("OK", color = confirmColor, fontWeight = FontWeight.Bold)
                    }
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

private fun buildTask(
    existing: Task?,
    title: String,
    category: String,
    description: String,
    status: TaskStatus,
    priority: Priority,
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
            status      = status,
            priority    = priority
        )
    } else {
        val newProgress = if (status != existing.status) initialProgress else existing.progress
        existing.copy(
            title       = title,
            description = description,
            category    = category,
            startTime   = startMillis,
            endTime     = endMillis,
            status      = status,
            priority    = priority,
            progress    = newProgress
        )
    }
}

private data class InitialFormValues(
    val title: String,
    val category: String,
    val description: String,
    val status: TaskStatus,
    val priority: Priority,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int
) {
    companion object {
        fun from(task: Task?, selectedDate: LocalDate): InitialFormValues {
            if (task == null) {
                val start = smartDefaultStart(selectedDate)
                val end   = start.plusHours(1).let {
                    // Avoid wrapping past 23:59
                    if (it.hour == 0 && start.hour == 23) LocalTime.of(23, 30) else it
                }
                return InitialFormValues(
                    title       = "",
                    category    = "",
                    description = "",
                    status      = TaskStatus.PENDING,
                    priority    = Priority.MEDIUM,
                    startHour   = start.hour,
                    startMinute = start.minute,
                    endHour     = end.hour,
                    endMinute   = end.minute
                )
            }
            val zone  = ZoneId.systemDefault()
            val start = java.time.Instant.ofEpochMilli(task.startTime).atZone(zone).toLocalTime()
            val end   = java.time.Instant.ofEpochMilli(task.endTime).atZone(zone).toLocalTime()
            return InitialFormValues(
                title       = task.title,
                category    = task.category,
                description = task.description,
                status      = task.status,
                priority    = task.priority,
                startHour   = start.hour,
                startMinute = start.minute,
                endHour     = end.hour,
                endMinute   = end.minute
            )
        }
    }
}

/**
 * Smart "fresh start" suggestion for a new task's start time.
 *  - Today          → next whole hour after now (capped at 23:00).
 *  - Future date    → 08:00 (a clean morning start).
 *  - Past date      → 08:00 (the form will block save anyway).
 */
private fun smartDefaultStart(date: LocalDate): LocalTime {
    val today = LocalDate.now()
    if (date != today) return LocalTime.of(8, 0)

    val now = LocalTime.now()
    val nextHour = if (now.minute == 0) now.hour else now.hour + 1
    return when {
        nextHour >= 23 -> LocalTime.of(23, 0)
        else           -> LocalTime.of(nextHour, 0)
    }
}

@Composable
private fun PriorityChipChoice(
    priority: Priority,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val accent = when (priority) {
        Priority.LOW    -> MintGreen
        Priority.MEDIUM -> Lavender
        Priority.HIGH   -> Cyan
    }
    val bg     = if (isSelected) accent else BgChip
    val fg     = if (isSelected) {
        when (priority) {
            Priority.LOW    -> OnMint
            Priority.MEDIUM -> OnLavender
            Priority.HIGH   -> OnCyan
        }
    } else accent
    val border = if (isSelected) accent else BorderSubtle
    val label  = when (priority) {
        Priority.LOW    -> "Low"
        Priority.MEDIUM -> "Medium"
        Priority.HIGH   -> "High"
    }

    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(20.dp),
        color    = bg,
        border   = BorderStroke(1.dp, border),
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) fg.copy(alpha = 0.7f) else accent)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text       = label,
                color      = fg,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
