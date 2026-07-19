package com.nutritrack.app.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nutritrack.app.MainActivity
import com.nutritrack.app.R
import com.nutritrack.app.data.repository.FoodDiaryRepository
import com.nutritrack.app.ui.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.Instant
import java.time.LocalTime

@HiltWorker
class MealGapReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val foodDiaryRepository: FoodDiaryRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val hour = LocalTime.now().hour
        if (hour !in WINDOW_START_HOUR until WINDOW_END_HOUR) return Result.success()

        val thresholdHours = inputData.getInt(INPUT_KEY_THRESHOLD_HOURS, DEFAULT_THRESHOLD_HOURS)
        val mostRecent = foodDiaryRepository.getMostRecentEntry()
        val hoursSinceLastMeal = mostRecent?.let { Duration.between(it.timestamp, Instant.now()).toHours() }
        if (hoursSinceLastMeal == null || hoursSinceLastMeal >= thresholdHours) {
            showNotification()
        }
        return Result.success()
    }

    private fun showNotification() {
        val context = applicationContext
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_NAVIGATE_TO, Screen.Diary.route)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_REQUEST_CODE,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Meal reminder")
            .setContentText("You haven't logged anything in a while — don't forget to track your meals.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "meal_gap_reminder"
        const val INPUT_KEY_THRESHOLD_HOURS = "threshold_hours"
        const val DEFAULT_THRESHOLD_HOURS = 4
        private const val NOTIFICATION_ID = 1006
        private const val NOTIFICATION_REQUEST_CODE = 2006

        // Only surfaces the reminder during waking hours - 8am up to (not including) 9pm.
        private const val WINDOW_START_HOUR = 8
        private const val WINDOW_END_HOUR = 21
    }
}
