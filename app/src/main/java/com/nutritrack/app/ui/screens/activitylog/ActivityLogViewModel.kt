package com.nutritrack.app.ui.screens.activitylog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.data.local.entity.ActivityEntryEntity
import com.nutritrack.app.data.local.entity.ActivityType
import com.nutritrack.app.data.local.entity.DailyLogEntity
import com.nutritrack.app.data.local.entity.Intensity
import com.nutritrack.app.data.repository.ActivityLogRepository
import com.nutritrack.app.data.repository.DailyLogRepository
import com.nutritrack.app.data.repository.FoodDiaryRepository
import com.nutritrack.app.data.repository.UserProfileRepository
import com.nutritrack.app.domain.activity.ActivityCalorieCalculator
import com.nutritrack.app.domain.activity.ActivityMetDefaults
import com.nutritrack.app.domain.activity.HydrationTipProvider
import com.nutritrack.app.domain.activity.metMultiplier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

private const val DEFAULT_DURATION_MINUTES = 30

data class ActivityLogUiState(
    val todaysActivities: List<ActivityEntryEntity> = emptyList(),
    val totalCaloriesBurnedToday: Double = 0.0,
    val totalCaloriesConsumedToday: Double = 0.0,
    val recentActivities: List<ActivityEntryEntity> = emptyList(),
    val selectedActivityType: ActivityType? = null,
    val durationMinutes: Int = DEFAULT_DURATION_MINUTES,
    val intensity: Intensity = Intensity.MODERATE,
    val weightKg: Double? = null,
    val dailyCalorieTarget: Int? = null,
    val isSaving: Boolean = false,
) {
    val baseMet: Double?
        get() = selectedActivityType?.let(ActivityMetDefaults::baseMet)

    val adjustedMet: Double?
        get() = baseMet?.let { it * intensity.metMultiplier() }

    val pendingCaloriesBurned: Double?
        get() {
            val met = adjustedMet ?: return null
            val weight = weightKg ?: return null
            return ActivityCalorieCalculator.calculateCaloriesBurned(met, weight, durationMinutes)
        }

    val adjustedDailyCalorieTarget: Double?
        get() {
            val target = dailyCalorieTarget ?: return null
            return target + totalCaloriesBurnedToday + (pendingCaloriesBurned ?: 0.0)
        }

    val remainingCaloriesAfterActivity: Double?
        get() = adjustedDailyCalorieTarget?.let { it - totalCaloriesConsumedToday }

    val hydrationTip: String?
        get() = selectedActivityType?.let { HydrationTipProvider.tipFor(it, intensity) }

    val canSave: Boolean
        get() = selectedActivityType != null && durationMinutes > 0 && weightKg != null
}

@HiltViewModel
class ActivityLogViewModel @Inject constructor(
    private val activityLogRepository: ActivityLogRepository,
    private val foodDiaryRepository: FoodDiaryRepository,
    private val userProfileRepository: UserProfileRepository,
    private val dailyLogRepository: DailyLogRepository,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()

    private val _uiState = MutableStateFlow(ActivityLogUiState())
    val uiState: StateFlow<ActivityLogUiState> = _uiState.asStateFlow()

    init {
        combine(
            activityLogRepository.observeEntriesForDate(today),
            activityLogRepository.observeTotalCaloriesBurnedForDate(today),
            activityLogRepository.observeRecentEntries(5),
            foodDiaryRepository.observeTotalCaloriesForDate(today),
            userProfileRepository.observeProfile(),
        ) { entries, burned, recent, consumed, profile ->
            _uiState.update {
                it.copy(
                    todaysActivities = entries,
                    totalCaloriesBurnedToday = burned,
                    recentActivities = recent,
                    totalCaloriesConsumedToday = consumed,
                    weightKg = profile?.weightKg,
                    dailyCalorieTarget = profile?.dailyCalorieTarget?.takeIf { target -> target > 0 },
                )
            }
        }.launchIn(viewModelScope)
    }

    fun selectActivityType(activityType: ActivityType) =
        _uiState.update { it.copy(selectedActivityType = activityType) }

    fun updateDurationMinutes(minutes: Int) =
        _uiState.update { it.copy(durationMinutes = minutes.coerceAtLeast(0)) }

    fun selectIntensity(intensity: Intensity) = _uiState.update { it.copy(intensity = intensity) }

    fun logActivity() {
        val state = _uiState.value
        val activityType = state.selectedActivityType ?: return
        val met = state.adjustedMet ?: return
        val caloriesBurned = state.pendingCaloriesBurned ?: return
        if (state.durationMinutes <= 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            activityLogRepository.logActivity(
                ActivityEntryEntity(
                    date = today,
                    activityType = activityType,
                    durationMinutes = state.durationMinutes,
                    intensity = state.intensity,
                    metValue = met,
                    caloriesBurned = caloriesBurned,
                    timestamp = Instant.now(),
                ),
            )
            recalculateDailyLog(today)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    selectedActivityType = null,
                    durationMinutes = DEFAULT_DURATION_MINUTES,
                    intensity = Intensity.MODERATE,
                )
            }
        }
    }

    // Repeats a past entry today, recalculating calories against the current weight
    // rather than reusing the original figure.
    fun reLogActivity(entry: ActivityEntryEntity) {
        viewModelScope.launch {
            val weight = _uiState.value.weightKg ?: userProfileRepository.getProfile()?.weightKg ?: return@launch
            val caloriesBurned = ActivityCalorieCalculator.calculateCaloriesBurned(
                entry.metValue,
                weight,
                entry.durationMinutes,
            )
            activityLogRepository.logActivity(
                entry.copy(id = 0, date = today, caloriesBurned = caloriesBurned, timestamp = Instant.now()),
            )
            recalculateDailyLog(today)
        }
    }

    private suspend fun recalculateDailyLog(date: LocalDate) {
        val consumed = foodDiaryRepository.observeTotalCaloriesForDate(date).first()
        val burned = activityLogRepository.observeTotalCaloriesBurnedForDate(date).first()
        val target = userProfileRepository.getProfile()?.dailyCalorieTarget?.takeIf { it > 0 }
        val net = consumed - burned
        dailyLogRepository.upsert(
            DailyLogEntity(
                date = date,
                totalCaloriesConsumed = consumed,
                totalActivityCaloriesBurned = burned,
                netCalories = net,
                isComplete = false,
                targetHit = target != null && net <= target,
            ),
        )
    }
}
