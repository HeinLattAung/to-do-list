package com.example.todolist

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.todolist.reminder.TaskReminderWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Hilt entry-point + WorkManager configuration.
 *
 * `Configuration.Provider` is required so that WorkManager picks up
 * Hilt's [HiltWorkerFactory], which lets [TaskReminderWorker] receive
 * `@Inject` dependencies (the TaskRepository).
 *
 * Don't forget to disable the default WorkManager initializer in
 * AndroidManifest.xml (see snippet below).
 */
@HiltAndroidApp
class TodoApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Pre-create the notification channel so the very first reminder
        // posts cleanly without races.
        TaskReminderWorker.ensureChannel(this)
    }
}
