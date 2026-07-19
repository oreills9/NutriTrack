package com.nutritrack.app.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nutritrack.app.MainActivity
import com.nutritrack.app.R
import com.nutritrack.app.data.local.entity.FoodEntryEntity
import com.nutritrack.app.data.local.entity.MealSlot
import com.nutritrack.app.ui.navigation.Screen
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private val REQUIRED_MEAL_SLOTS = setOf(MealSlot.MORNING, MealSlot.AFTERNOON, MealSlot.EVENING)

// There's no existing app-wide "is today's food log complete" flag to reuse - DailyLogEntity.isComplete
// is written but never actually set to true anywhere. This defines it just for the badge feature:
// complete once each of the three core meal slots has at least one entry (the optional
// ACTIVITY_SNACK slot doesn't count towards it).
internal fun isFoodLogComplete(entries: List<FoodEntryEntity>): Boolean =
    REQUIRED_MEAL_SLOTS.all { slot -> entries.any { it.mealSlot == slot } }

// Android has no direct "set an icon badge number" API outside of notifications - the launcher
// derives the badge from active notification count (and, on launchers that support it, the
// number set via setNumber()). So the badge is implemented as a single low-importance notification
// that's posted/cancelled as completion status changes, rather than a separate badge mechanism.
//
// Deliberately takes no repository dependency: RoomFoodDiaryRepository needs to call this after
// every save, so this can't depend on FoodDiaryRepository without creating a Hilt dependency cycle.
// Callers compute completeness themselves (via isFoodLogComplete above) and just tell this class
// show/clear.
@Singleton
class DailyLogBadgeManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun show() {
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
            REQUEST_CODE,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Today's food log is incomplete")
            .setContentText("Tap to finish logging today's meals.")
            .setNumber(1)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    fun clear() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    companion object {
        const val CHANNEL_ID = "daily_log_badge"
        private const val NOTIFICATION_ID = 1007
        private const val REQUEST_CODE = 2007
    }
}
