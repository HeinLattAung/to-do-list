package com.example.todolist.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todolist.MainActivity
import com.example.todolist.R
import com.example.todolist.data.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Fires a notification reminding the user that a task is starting.
 *
 * Receives the task's id via WorkManager input data, looks the row up in
 * the repository so the notification always shows the latest title/category
 * (relevant if the task was edited after scheduling).
 */
@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: TaskRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(KEY_TASK_ID, -1L)
        if (taskId == -1L) return Result.failure()

        val task = repository.getById(taskId) ?: return Result.success()  // task was deleted

        // Skip if user already cancelled / completed it before the alarm fired
        val skipStatuses = setOf(
            com.example.todolist.data.local.entity.TaskStatus.CANCELLED,
            com.example.todolist.data.local.entity.TaskStatus.COMPLETED
        )
        if (task.status in skipStatuses) return Result.success()

        showNotification(
            id       = task.id.toInt(),
            title    = task.title,
            category = task.category.ifBlank { "To-Do" }
        )
        return Result.success()
    }

    private fun showNotification(id: Int, title: String, category: String) {
        ensureChannel(context)

        // Tap the notification → open the app
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingFlags =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val tapPending = PendingIntent.getActivity(context, id, tapIntent, pendingFlags)

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)         // provide a 24dp white icon
            .setContentTitle(title)
            .setContentText("Starting now • $category")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your task \"$title\" ($category) is starting now.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(tapPending)
            .build()

        // POST_NOTIFICATIONS permission is required on Android 13+
        val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            NotificationManagerCompat.from(context).notify(id, notif)
        }
    }

    companion object {
        const val WORK_NAME_PREFIX = "task_reminder_"
        const val KEY_TASK_ID      = "task_id"
        const val CHANNEL_ID       = "task_reminders"
        const val CHANNEL_NAME     = "Task Reminders"

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mgr = context.getSystemService(NotificationManager::class.java)
                if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Notifications for upcoming tasks"
                    }
                    mgr.createNotificationChannel(channel)
                }
            }
        }
    }
}
