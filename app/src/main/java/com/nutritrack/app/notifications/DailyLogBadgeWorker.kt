package com.nutritrack.app.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nutritrack.app.data.repository.FoodDiaryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class DailyLogBadgeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val foodDiaryRepository: FoodDiaryRepository,
    private val badgeManager: DailyLogBadgeManager,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val entries = foodDiaryRepository.observeEntriesForDate(LocalDate.now()).first()
        if (isFoodLogComplete(entries)) badgeManager.clear() else badgeManager.show()
        return Result.success()
    }
}
