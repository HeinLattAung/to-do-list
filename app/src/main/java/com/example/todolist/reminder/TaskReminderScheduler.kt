package com.example.todolist.reminder

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.todolist.data.local.entity.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules / cancels [TaskReminderWorker] for a given Task.
 *
 * Uses WorkManager (preferred over AlarmManager because it survives
 * device reboots automatically and respects Doze / battery rules).
 *
 * For *exact* alarms (e.g. medication reminders) you'd need
 * AlarmManager.setExactAndAllowWhileIdle + the SCHEDULE_EXACT_ALARM
 * permission. WorkManager's accuracy is adequate for typical to-dos.
 */
@Singleton
class TaskReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager: WorkManager = WorkManager.getInstance(context)

    /**
     * Cancels any previously scheduled work for this task and (re)enqueues
     * a one-time worker that fires at [Task.startTime] (epoch millis).
     *
     * If startTime is already in the past — does nothing.
     */
    fun schedule(task: Task) {
        val workName = uniqueName(task.id)
        cancel(task.id)

        val delayMs = task.startTime - System.currentTimeMillis()
        if (delayMs <= 0) return                          // start time is in the past

        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putLong(TaskReminderWorker.KEY_TASK_ID, task.id)
                    .build()
            )
            .addTag(TAG_TASK_REMINDER)
            .build()

        workManager.enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.REPLACE,        // editing a task replaces the old reminder
            request
        )
    }

    fun cancel(taskId: Long) {
        workManager.cancelUniqueWork(uniqueName(taskId))
    }

    private fun uniqueName(taskId: Long) = "${TaskReminderWorker.WORK_NAME_PREFIX}$taskId"

    companion object {
        private const val TAG_TASK_REMINDER = "task_reminder"
    }
}
