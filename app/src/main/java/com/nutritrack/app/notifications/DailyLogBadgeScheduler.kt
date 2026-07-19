package com.nutritrack.app.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

// No user-facing toggle for this one - it's silent maintenance, same as SupplementResetScheduler.
object DailyLogBadgeScheduler {

    private const val UNIQUE_WORK_NAME = "daily_log_badge_check"
    private val CHECK_TIME: LocalTime = LocalTime.of(20, 0)

    fun schedule(context: Context) {
        val initialDelay = Duration.between(LocalDateTime.now(), nextTimeToday(CHECK_TIME))
        val request = PeriodicWorkRequestBuilder<DailyLogBadgeWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay.toMinutes().coerceAtLeast(0), TimeUnit.MINUTES)
            .build()

        // KEEP: re-running schedule() on every app start must not reset the 8pm timing.
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
