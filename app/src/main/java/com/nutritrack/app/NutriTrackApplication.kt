package com.nutritrack.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.nutritrack.app.notifications.BloodPressureReminderScheduler
import com.nutritrack.app.notifications.BloodPressureReminderWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NutriTrackApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        BloodPressureReminderScheduler.schedule(this)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            BloodPressureReminderWorker.CHANNEL_ID,
            "Blood Pressure Reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Weekly reminder to log your blood pressure"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
