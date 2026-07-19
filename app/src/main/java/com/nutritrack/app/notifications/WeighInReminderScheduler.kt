package com.nutritrack.app.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object WeighInReminderScheduler {

    private const val UNIQUE_WORK_NAME = "weigh_in_sunday_reminder"

    fun schedule(
        context: Context,
        time: LocalTime,
        policy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
    ) {
        val initialDelay = Duration.between(LocalDateTime.now(), nextSundayAt(time))
        val request = PeriodicWorkRequestBuilder<WeighInReminderWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(initialDelay.toMinutes().coerceAtLeast(0), TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, policy, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }
}
