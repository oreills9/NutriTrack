package com.nutritrack.app.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.nutritrack.app.data.local.entity.SupplementEntryEntity
import com.nutritrack.app.data.prefs.AppPreferencesRepository
import com.nutritrack.app.data.repository.SupplementsRepository
import com.nutritrack.app.notifications.BloodPressureReminderWorker
import com.nutritrack.app.notifications.MealGapReminderWorker
import com.nutritrack.app.notifications.SupplementReminderWorker
import com.nutritrack.app.notifications.WeighInReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Debug-only helper: fires the real notification workers immediately via a one-time WorkManager
// request instead of waiting for their real schedule, so their content/behavior can be checked by
// hand. Enqueues the actual production Worker classes (respecting their real internal gating, e.g.
// the meal gap window or "already taken" checks), not a separate copy of the logic. Only ever
// surfaced from DeveloperTestSection, which is gated behind BuildConfig.DEBUG.
@HiltViewModel
class DeveloperTestViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supplementsRepository: SupplementsRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    val supplements: StateFlow<List<SupplementEntryEntity>> = supplementsRepository.observeChecklist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun triggerMealGapReminder() {
        viewModelScope.launch {
            val hours = appPreferencesRepository.mealGapReminderHours.first()
            val inputData = Data.Builder().putInt(MealGapReminderWorker.INPUT_KEY_THRESHOLD_HOURS, hours).build()
            enqueue<MealGapReminderWorker>(inputData)
        }
    }

    fun triggerBpReminder() = enqueue<BloodPressureReminderWorker>()

    fun triggerWeighInReminder() = enqueue<WeighInReminderWorker>()

    fun triggerSupplementReminder(supplementId: Long) {
        val inputData = Data.Builder().putLong(SupplementReminderWorker.INPUT_KEY_SUPPLEMENT_ID, supplementId).build()
        enqueue<SupplementReminderWorker>(inputData)
    }

    private inline fun <reified T : ListenableWorker> enqueue(inputData: Data = Data.EMPTY) {
        val request = OneTimeWorkRequestBuilder<T>().setInputData(inputData).build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
