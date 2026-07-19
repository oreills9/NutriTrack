package com.nutritrack.app.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object MealGapReminderScheduler {

    private const val UNIQUE_WORK_NAME = "meal_gap_reminder"

    // Checked hourly rather than on a fixed daily time, since this is triggered by elapsed time
    // since the last FoodEntry rather than a time of day.
    fun schedule(
        context: Context,
        thresholdHours: Int,
        policy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
    ) {
        val inputData = Data.Builder()
            .putInt(MealGapReminderWorker.INPUT_KEY_THRESHOLD_HOURS, thresholdHours)
            .build()
        val request = PeriodicWorkRequestBuilder<MealGapReminderWorker>(1, TimeUnit.HOURS)
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, policy, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }
}
