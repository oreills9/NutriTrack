package com.nutritrack.app.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

// Each supplement gets its own daily job at its own time, keyed by supplement id, since every
// supplement can have a different reminder time (SupplementEntryEntity.timeOfDay).
object SupplementReminderScheduler {

    private fun uniqueWorkName(supplementId: Long) = "supplement_reminder_$supplementId"

    fun schedule(
        context: Context,
        supplementId: Long,
        time: LocalTime,
        policy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
    ) {
        val initialDelay = Duration.between(LocalDateTime.now(), nextTimeToday(time))
        val inputData = Data.Builder()
            .putLong(SupplementReminderWorker.INPUT_KEY_SUPPLEMENT_ID, supplementId)
            .build()
        val request = PeriodicWorkRequestBuilder<SupplementReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay.toMinutes().coerceAtLeast(0), TimeUnit.MINUTES)
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(uniqueWorkName(supplementId), policy, request)
    }

    fun cancel(context: Context, supplementId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName(supplementId))
    }
}
