package com.nutritrack.app.ui.screens.activitylog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.data.local.entity.ActivityEntryEntity
import com.nutritrack.app.data.local.entity.ActivityType
import com.nutritrack.app.data.local.entity.Intensity
import com.nutritrack.app.data.repository.ActivityLogRepository
import com.nutritrack.app.data.repository.UserProfileRepository
import com.nutritrack.app.domain.activity.ActivityCalorieCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

data class ActivityLogUiState(
    val todaysActivities: List<ActivityEntryEntity> = emptyList(),
    val totalCaloriesBurnedToday: Double = 0.0,
)

@HiltViewModel
class ActivityLogViewModel @Inject constructor(
    private val activityLogRepository: ActivityLogRepository,
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()

    val uiState: StateFlow<ActivityLogUiState> = combine(
        activityLogRepository.observeEntriesForDate(today),
        activityLogRepository.observeTotalCaloriesBurnedForDate(today),
    ) { entries, total ->
        ActivityLogUiState(todaysActivities = entries, totalCaloriesBurnedToday = total)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActivityLogUiState())

    // MET x weight (kg) x duration (hours), using the user's current weight.
    suspend fun calculateCaloriesBurned(metValue: Double, durationMinutes: Int): Double {
        val weightKg = userProfileRepository.getProfile()?.weightKg ?: return 0.0
        return ActivityCalorieCalculator.calculateCaloriesBurned(metValue, weightKg, durationMinutes)
    }

    fun logActivity(activityType: ActivityType, durationMinutes: Int, intensity: Intensity, metValue: Double) {
        viewModelScope.launch {
            val caloriesBurned = calculateCaloriesBurned(metValue, durationMinutes)
            activityLogRepository.logActivity(
                ActivityEntryEntity(
                    date = today,
                    activityType = activityType,
                    durationMinutes = durationMinutes,
                    intensity = intensity,
                    metValue = metValue,
                    caloriesBurned = caloriesBurned,
                    timestamp = Instant.now(),
                ),
            )
        }
    }

    fun updateEntry(entry: ActivityEntryEntity) {
        viewModelScope.launch { activityLogRepository.updateEntry(entry) }
    }

    fun deleteEntry(entry: ActivityEntryEntity) {
        viewModelScope.launch { activityLogRepository.deleteEntry(entry) }
    }
}
