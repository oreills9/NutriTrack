package com.nutritrack.app.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object SupplementResetScheduler {

    private const val UNIQUE_WORK_NAME = "supplement_daily_reset"

    fun schedule(context: Context) {
        val now = LocalDateTime.now()
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
        val initialDelay = Duration.between(now, nextMidnight)

        val request = PeriodicWorkRequestBuilder<SupplementResetWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay.toMinutes().coerceAtLeast(0), TimeUnit.MINUTES)
            .build()

        // KEEP: re-running schedule() on every app start must not reset an already-scheduled
        // reset's timing (see NutriTrackApplication).
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
