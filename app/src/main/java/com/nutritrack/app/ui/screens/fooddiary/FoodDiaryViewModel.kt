package com.nutritrack.app.ui.screens.fooddiary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.data.local.entity.FoodEntryEntity
import com.nutritrack.app.data.local.entity.MealSlot
import com.nutritrack.app.data.repository.ActivityLogRepository
import com.nutritrack.app.data.repository.FoodDiaryRepository
import com.nutritrack.app.data.repository.UserProfileRepository
import com.nutritrack.app.domain.calorie.CaloriePaceIndicator
import com.nutritrack.app.domain.calorie.CaloriePacingCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class FoodDiaryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val entriesByMealSlot: Map<MealSlot, List<FoodEntryEntity>> = MealSlot.entries.associateWith { emptyList() },
    val totalCaloriesConsumed: Double = 0.0,
    val totalCaloriesBurned: Double = 0.0,
    val dailyCalorieTarget: Int? = null,
    val remainingCalories: Double? = null,
    val netCalories: Double? = null,
    val remainingAdjustedCalories: Double? = null,
    val paceIndicator: CaloriePaceIndicator? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FoodDiaryViewModel @Inject constructor(
    private val foodDiaryRepository: FoodDiaryRepository,
    private val activityLogRepository: ActivityLogRepository,
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<FoodDiaryUiState> = selectedDate.flatMapLatest { date ->
        combine(
            foodDiaryRepository.observeEntriesForDate(date),
            foodDiaryRepository.observeTotalCaloriesForDate(date),
            activityLogRepository.observeTotalCaloriesBurnedForDate(date),
            userProfileRepository.observeProfile(),
        ) { entries, consumed, burned, profile ->
            val target = profile?.dailyCalorieTarget?.takeIf { it > 0 }
            val net = consumed - burned
            FoodDiaryUiState(
                selectedDate = date,
                entriesByMealSlot = MealSlot.entries.associateWith { slot -> entries.filter { it.mealSlot == slot } },
                totalCaloriesConsumed = consumed,
                totalCaloriesBurned = burned,
                dailyCalorieTarget = target,
                remainingCalories = target?.let { it - consumed },
                netCalories = net,
                remainingAdjustedCalories = target?.let { it - net },
                paceIndicator = target?.let { CaloriePacingCalculator.calculatePaceIndicator(consumed, it) },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FoodDiaryUiState())

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun logFood(entry: FoodEntryEntity) {
        viewModelScope.launch { foodDiaryRepository.logFood(entry) }
    }

    fun updateEntry(entry: FoodEntryEntity) {
        viewModelScope.launch { foodDiaryRepository.updateEntry(entry) }
    }

    fun deleteEntry(entry: FoodEntryEntity) {
        viewModelScope.launch { foodDiaryRepository.deleteEntry(entry) }
    }
}
