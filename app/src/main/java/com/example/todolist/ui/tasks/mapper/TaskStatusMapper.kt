package com.example.todolist.ui.tasks.mapper

import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Aliases keep the call-site readable when both enums are in scope.
private typealias DataStatus = com.example.todolist.data.local.entity.TaskStatus
private typealias UiStatus   = com.example.taskcard.TaskStatus

/* =============================================================
 *  Status mapping
 *  -----------------------------------------------------------
 *  data.TaskStatus  →  ui.TaskStatus
 *  The UI enum carries label + pastel background + accent color,
 *  so the card composable can read those directly.
 * ============================================================= */

fun DataStatus.toUiStatus(): UiStatus = when (this) {
    DataStatus.COMPLETED -> UiStatus.COMPLETED
    DataStatus.RUNNING   -> UiStatus.RUNNING
    DataStatus.PENDING   -> UiStatus.PENDING
    DataStatus.CANCELLED -> UiStatus.CANCELLED
}

/** Inverse mapping — handy in the AddTaskSheet's Save button. */
fun UiStatus.toDataStatus(): DataStatus = when (this) {
    UiStatus.COMPLETED -> DataStatus.COMPLETED
    UiStatus.RUNNING   -> DataStatus.RUNNING
    UiStatus.PENDING   -> DataStatus.PENDING
    UiStatus.CANCELLED -> DataStatus.CANCELLED
}

/* =============================================================
 *  Convenience getters used by the card UI
 * ============================================================= */

/** Accent color used by the status tab, progress bar and percentage. */
val DataStatus.accent: Color get() = toUiStatus().accent

/** Display label, e.g. "Completed". */
val DataStatus.label: String get() = toUiStatus().label

/* =============================================================
 *  Time formatter — epoch millis  →  "10:00 AM"
 * ============================================================= */
private val hourMinuteFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("hh:mm a")
        .withZone(ZoneId.systemDefault())

fun Long.formatHourMinute(): String =
    hourMinuteFormatter.format(Instant.ofEpochMilli(this))
