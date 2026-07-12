package com.nutritrack.app.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

object BloodPressureReminderScheduler {

    private const val UNIQUE_WORK_NAME = "blood_pressure_sunday_reminder"
    private val REMINDER_TIME: LocalTime = LocalTime.of(9, 0)

    fun schedule(context: Context) {
        val initialDelay = Duration.between(LocalDateTime.now(), nextSundayReminderTime())

        val request = PeriodicWorkRequestBuilder<BloodPressureReminderWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(initialDelay.toMinutes().coerceAtLeast(0), TimeUnit.MINUTES)
            .build()

        // KEEP: re-running schedule() on every app start (see NutriTrackApplication) must not
        // reset an already-scheduled reminder's timing.
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private fun nextSundayReminderTime(): LocalDateTime {
        val now = LocalDateTime.now()
        val candidate = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).with(REMINDER_TIME)
        return if (candidate.isBefore(now)) candidate.plusWeeks(1) else candidate
    }
}
