package com.nutritrack.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.nutritrack.app.data.prefs.AppPreferencesRepository
import com.nutritrack.app.notifications.BloodPressureReminderScheduler
import com.nutritrack.app.notifications.BloodPressureReminderWorker
import com.nutritrack.app.notifications.MealGapReminderScheduler
import com.nutritrack.app.notifications.MealGapReminderWorker
import com.nutritrack.app.notifications.SupplementReminderScheduler
import com.nutritrack.app.notifications.SupplementReminderWorker
import com.nutritrack.app.notifications.WeighInReminderScheduler
import com.nutritrack.app.notifications.WeighInReminderWorker
import com.nutritrack.app.work.SupplementResetScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class NutriTrackApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var appPreferencesRepository: AppPreferencesRepository

    // Lives for the process lifetime, same as the Application itself - never needs cancelling.
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // androidx.work 2.11.2's Configuration.Provider is a Kotlin property (workManagerConfiguration),
    // not the getWorkManagerConfiguration() function shown in some older docs/samples.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        SupplementResetScheduler.schedule(this)
        applyStoredReminderPreferences()
    }

    // Re-applies whatever was last saved in Settings. Uses the default KEEP policy so this is a
    // safety net (e.g. first install, WorkManager DB cleared) rather than a reschedule on every
    // launch - Settings itself uses REPLACE when the user actually changes something.
    private fun applyStoredReminderPreferences() {
        applicationScope.launch {
            if (appPreferencesRepository.sundayBpReminderEnabled.first()) {
                BloodPressureReminderScheduler.schedule(this@NutriTrackApplication, appPreferencesRepository.sundayBpReminderTime.first())
            } else {
                BloodPressureReminderScheduler.cancel(this@NutriTrackApplication)
            }

            if (appPreferencesRepository.sundayWeighInReminderEnabled.first()) {
                WeighInReminderScheduler.schedule(this@NutriTrackApplication, appPreferencesRepository.sundayWeighInReminderTime.first())
            } else {
                WeighInReminderScheduler.cancel(this@NutriTrackApplication)
            }

            if (appPreferencesRepository.supplementReminderEnabled.first()) {
                SupplementReminderScheduler.schedule(this@NutriTrackApplication, appPreferencesRepository.supplementReminderTime.first())
            } else {
                SupplementReminderScheduler.cancel(this@NutriTrackApplication)
            }

            if (appPreferencesRepository.mealGapReminderEnabled.first()) {
                MealGapReminderScheduler.schedule(this@NutriTrackApplication, appPreferencesRepository.mealGapReminderHours.first())
            } else {
                MealGapReminderScheduler.cancel(this@NutriTrackApplication)
            }
        }
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                BloodPressureReminderWorker.CHANNEL_ID,
                "Blood Pressure Reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Weekly reminder to log your blood pressure" },
        )
        manager.createNotificationChannel(
            NotificationChannel(
                WeighInReminderWorker.CHANNEL_ID,
                "Weigh-In Reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Weekly reminder to log your weight" },
        )
        manager.createNotificationChannel(
            NotificationChannel(
                SupplementReminderWorker.CHANNEL_ID,
                "Supplement Reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Daily reminder for supplements or medication not yet taken" },
        )
        manager.createNotificationChannel(
            NotificationChannel(
                MealGapReminderWorker.CHANNEL_ID,
                "Meal Gap Reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Reminder when it's been a while since your last logged meal" },
        )
    }
}
