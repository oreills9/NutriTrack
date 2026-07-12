package com.nutritrack.app.ui.screens.profilesetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.data.local.entity.ActivityLevel
import com.nutritrack.app.data.local.entity.BiologicalSex
import com.nutritrack.app.data.local.entity.UnitSystem
import com.nutritrack.app.data.prefs.AppPreferencesRepository
import com.nutritrack.app.data.repository.UserProfile
import com.nutritrack.app.data.repository.UserProfileRepository
import com.nutritrack.app.domain.calorie.CalorieCalculator
import com.nutritrack.app.domain.calorie.MacroCalculator
import com.nutritrack.app.domain.calorie.MacroSplit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

data class ProfileSetupUiState(
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val age: Int? = null,
    val biologicalSex: BiologicalSex? = null,
    val activityLevel: ActivityLevel? = null,
    val preferredUnits: UnitSystem = UnitSystem.METRIC,
    val targetWeightKg: Double? = null,
    val goalDeadlineWeeks: Int? = null,
    val calculatedTdee: Int? = null,
    val dailyCalorieTarget: Int? = null,
    val macroSplit: MacroSplit? = null,
    val isSaving: Boolean = false,
    val isComplete: Boolean = false,
) {
    val isPersonalSectionValid: Boolean
        get() = heightCm != null && weightKg != null && age != null &&
            biologicalSex != null && activityLevel != null

    val isGoalSectionValid: Boolean
        get() = targetWeightKg != null && goalDeadlineWeeks != null
}

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    fun updateHeightCm(value: Double?) = _uiState.update { it.copy(heightCm = value) }

    fun updateWeightKg(value: Double?) = _uiState.update { it.copy(weightKg = value) }

    fun updateAge(value: Int?) = _uiState.update { it.copy(age = value) }

    fun updateBiologicalSex(value: BiologicalSex) = _uiState.update { it.copy(biologicalSex = value) }

    fun updateActivityLevel(value: ActivityLevel) = _uiState.update { it.copy(activityLevel = value) }

    fun updatePreferredUnits(value: UnitSystem) = _uiState.update { it.copy(preferredUnits = value) }

    fun updateTargetWeightKg(value: Double?) = _uiState.update { it.copy(targetWeightKg = value) }

    fun updateGoalDeadlineWeeks(value: Int?) = _uiState.update { it.copy(goalDeadlineWeeks = value) }

    // Recomputed live every time Section 3 is shown, so going back and changing an earlier
    // answer is always reflected. Nothing is persisted here - only completeSetup() writes
    // to Room/DataStore, once, at the very end of onboarding.
    fun calculatePreview() {
        val state = _uiState.value
        val height = state.heightCm ?: return
        val weight = state.weightKg ?: return
        val age = state.age ?: return
        val sex = state.biologicalSex ?: return
        val activityLevel = state.activityLevel ?: return
        val targetWeight = state.targetWeightKg ?: return
        val deadlineWeeks = state.goalDeadlineWeeks ?: return

        val bmr = CalorieCalculator.calculateBmr(
            weightKg = weight,
            heightCm = height,
            age = age,
            biologicalSex = sex,
        )
        val tdee = CalorieCalculator.calculateTdee(bmr, activityLevel)
        val target = CalorieCalculator.calculateDailyCalorieTarget(
            tdee = tdee,
            currentWeightKg = weight,
            targetWeightKg = targetWeight,
            goalDeadlineWeeks = deadlineWeeks,
        )
        _uiState.update {
            it.copy(
                calculatedTdee = tdee.roundToInt(),
                dailyCalorieTarget = target,
                macroSplit = MacroCalculator.calculateMacroSplit(target),
            )
        }
    }

    fun completeSetup() {
        val state = _uiState.value
        val heightCm = state.heightCm ?: return
        val weightKg = state.weightKg ?: return
        val age = state.age ?: return
        val biologicalSex = state.biologicalSex ?: return
        val activityLevel = state.activityLevel ?: return
        val targetWeightKg = state.targetWeightKg ?: return
        val goalDeadlineWeeks = state.goalDeadlineWeeks ?: return
        val dailyCalorieTarget = state.dailyCalorieTarget ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            userProfileRepository.saveProfile(
                UserProfile(
                    heightCm = heightCm,
                    weightKg = weightKg,
                    age = age,
                    biologicalSex = biologicalSex,
                    activityLevel = activityLevel,
                    preferredUnits = state.preferredUnits,
                    dailyCalorieTarget = dailyCalorieTarget,
                    targetWeightKg = targetWeightKg,
                    goalDeadlineWeeks = goalDeadlineWeeks,
                    dateCreated = LocalDate.now(),
                ),
            )
            appPreferencesRepository.setOnboardingComplete()
            _uiState.update { it.copy(isSaving = false, isComplete = true) }
        }
    }
}
