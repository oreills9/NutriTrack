package com.nutritrack.app.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nutritrack.app.data.repository.SupplementsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SupplementResetWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val supplementsRepository: SupplementsRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        supplementsRepository.resetDailyChecklist()
        return Result.success()
    }
}
