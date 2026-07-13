package com.nutritrack.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.nutritrack.app.notifications.BloodPressureReminderScheduler
import com.nutritrack.app.notifications.BloodPressureReminderWorker
import com.nutritrack.app.work.SupplementResetScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NutriTrackApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // androidx.work 2.11.2's Configuration.Provider is a Kotlin property (workManagerConfiguration),
    // not the getWorkManagerConfiguration() function shown in some older docs/samples.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        BloodPressureReminderScheduler.schedule(this)
        SupplementResetScheduler.schedule(this)
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
