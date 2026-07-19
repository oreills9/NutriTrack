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
import com.nutritrack.app.data.local.entity.SupplementEntryEntity
import com.nutritrack.app.data.repository.SupplementsRepository
import com.nutritrack.app.ui.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SupplementReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val supplementsRepository: SupplementsRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val supplementId = inputData.getLong(INPUT_KEY_SUPPLEMENT_ID, -1L)
        if (supplementId == -1L) return Result.failure()
        // The supplement may have been deleted since this job was scheduled - just no-op rather
        // than treating it as a failure the WorkManager retry policy would act on.
        val supplement = supplementsRepository.getById(supplementId) ?: return Result.success()
        if (!supplement.takenToday) showNotification(supplement)
        return Result.success()
    }

    private fun showNotification(supplement: SupplementEntryEntity) {
        val context = applicationContext
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_NAVIGATE_TO, Screen.Supplements.route)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_REQUEST_CODE_BASE + supplement.id.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time for ${supplement.name}")
            .setContentText(supplement.dosageNotes?.ifBlank { null } ?: "Mark it as taken in NutriTrack once you have.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Offset by supplement id so each supplement's notification is independent - taking one
        // doesn't dismiss another, and a later reminder doesn't overwrite an earlier untapped one.
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BASE + supplement.id.toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "supplement_reminder"
        const val INPUT_KEY_SUPPLEMENT_ID = "supplement_id"
        private const val NOTIFICATION_ID_BASE = 5000
        private const val NOTIFICATION_REQUEST_CODE_BASE = 6000
    }
}
