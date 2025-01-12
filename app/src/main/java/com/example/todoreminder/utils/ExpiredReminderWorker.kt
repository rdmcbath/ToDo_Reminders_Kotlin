package com.example.todoreminder.utils

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.example.todoreminder.data.repository.RepositoryProvider
import com.example.todoreminder.data.repository.ToDoRepository
import java.util.concurrent.TimeUnit

class ExpiredReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val todoRepository = RepositoryProvider.getRepository(context)

    override suspend fun doWork(): Result {
        // Check and update expired reminders
        todoRepository.checkAndUpdateExpiredReminders()
        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            val work = PeriodicWorkRequestBuilder<ExpiredReminderWorker>(
                1, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "check_expired_reminders",
                    ExistingPeriodicWorkPolicy.KEEP,
                    work
                )
        }
    }
}