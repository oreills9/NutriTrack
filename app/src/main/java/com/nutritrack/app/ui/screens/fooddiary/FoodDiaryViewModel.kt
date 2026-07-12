package com.nutritrack.app.ui.screens.fooddiary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.data.local.entity.FoodEntryEntity
import com.nutritrack.app.data.local.entity.MealSlot
import com.nutritrack.app.data.repository.FoodDiaryRepository
import com.nutritrack.app.data.repository.UserProfileRepository
import com.nutritrack.app.domain.calorie.CaloriePaceIndicator
import com.nutritrack.app.domain.calorie.CaloriePacingCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class FoodDiaryUiState(
    val entriesByMealSlot: Map<MealSlot, List<FoodEntryEntity>> = MealSlot.entries.associateWith { emptyList() },
    val totalCaloriesConsumed: Double = 0.0,
    val dailyCalorieTarget: Int? = null,
    val remainingCalories: Double? = null,
    val paceIndicator: CaloriePaceIndicator? = null,
)

@HiltViewModel
class FoodDiaryViewModel @Inject constructor(
    private val foodDiaryRepository: FoodDiaryRepository,
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()

    val uiState: StateFlow<FoodDiaryUiState> = combine(
        foodDiaryRepository.observeEntriesForDate(today),
        foodDiaryRepository.observeTotalCaloriesForDate(today),
        userProfileRepository.observeProfile(),
    ) { entries, totalCalories, profile ->
        val target = profile?.dailyCalorieTarget?.takeIf { it > 0 }
        FoodDiaryUiState(
            entriesByMealSlot = MealSlot.entries.associateWith { slot -> entries.filter { it.mealSlot == slot } },
            totalCaloriesConsumed = totalCalories,
            dailyCalorieTarget = target,
            remainingCalories = target?.let { it - totalCalories },
            paceIndicator = target?.let { CaloriePacingCalculator.calculatePaceIndicator(totalCalories, it) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FoodDiaryUiState())

    fun logFood(entry: FoodEntryEntity) {
        viewModelScope.launch { foodDiaryRepository.logFood(entry) }
    }

    fun updateEntry(entry: FoodEntryEntity) {
        viewModelScope.launch { foodDiaryRepository.updateEntry(entry) }
    }

    fun deleteEntry(entry: FoodEntryEntity) {
        viewModelScope.launch { foodDiaryRepository.deleteEntry(entry) }
    }

    fun calculatePacingIndicator(): CaloriePaceIndicator? {
        val state = uiState.value
        val target = state.dailyCalorieTarget ?: return null
        return CaloriePacingCalculator.calculatePaceIndicator(state.totalCaloriesConsumed, target)
    }
}
