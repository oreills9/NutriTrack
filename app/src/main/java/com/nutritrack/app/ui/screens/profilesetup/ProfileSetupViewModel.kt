package com.nutritrack.app.ui.screens.profilesetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.data.local.entity.ActivityLevel
import com.nutritrack.app.data.local.entity.BiologicalSex
import com.nutritrack.app.data.local.entity.UnitSystem
import com.nutritrack.app.data.repository.UserProfile
import com.nutritrack.app.data.repository.UserProfileRepository
import com.nutritrack.app.domain.calorie.CalorieCalculator
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
    val isSaving: Boolean = false,
) {
    val isFormValid: Boolean
        get() = heightCm != null && weightKg != null && age != null &&
            biologicalSex != null && activityLevel != null &&
            targetWeightKg != null && goalDeadlineWeeks != null
}

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userProfileRepository.getProfile()?.let { profile ->
                _uiState.update {
                    it.copy(
                        heightCm = profile.heightCm,
                        weightKg = profile.weightKg,
                        age = profile.age,
                        biologicalSex = profile.biologicalSex,
                        activityLevel = profile.activityLevel,
                        preferredUnits = profile.preferredUnits,
                        targetWeightKg = profile.targetWeightKg,
                        goalDeadlineWeeks = profile.goalDeadlineWeeks,
                        dailyCalorieTarget = profile.dailyCalorieTarget.takeIf { target -> target > 0 },
                    )
                }
            }
        }
    }

    fun updateHeightCm(value: Double) = _uiState.update { it.copy(heightCm = value) }

    fun updateWeightKg(value: Double) = _uiState.update { it.copy(weightKg = value) }

    fun updateAge(value: Int) = _uiState.update { it.copy(age = value) }

    fun updateBiologicalSex(value: BiologicalSex) = _uiState.update { it.copy(biologicalSex = value) }

    fun updateActivityLevel(value: ActivityLevel) = _uiState.update { it.copy(activityLevel = value) }

    fun updatePreferredUnits(value: UnitSystem) = _uiState.update { it.copy(preferredUnits = value) }

    fun updateTargetWeightKg(value: Double) = _uiState.update { it.copy(targetWeightKg = value) }

    fun updateGoalDeadlineWeeks(value: Int) = _uiState.update { it.copy(goalDeadlineWeeks = value) }

    fun saveProfile() {
        val state = _uiState.value
        val heightCm = state.heightCm ?: return
        val weightKg = state.weightKg ?: return
        val age = state.age ?: return
        val biologicalSex = state.biologicalSex ?: return
        val activityLevel = state.activityLevel ?: return
        val targetWeightKg = state.targetWeightKg ?: return
        val goalDeadlineWeeks = state.goalDeadlineWeeks ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val existing = userProfileRepository.getProfile()
            val profile = UserProfile(
                heightCm = heightCm,
                weightKg = weightKg,
                age = age,
                biologicalSex = biologicalSex,
                activityLevel = activityLevel,
                preferredUnits = state.preferredUnits,
                dailyCalorieTarget = existing?.dailyCalorieTarget ?: 0,
                targetWeightKg = targetWeightKg,
                goalDeadlineWeeks = goalDeadlineWeeks,
                dateCreated = existing?.dateCreated ?: LocalDate.now(),
            )
            userProfileRepository.saveProfile(profile)
            applyCalorieGoal(profile)
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    // Public entry point so the screen can trigger/retry the calculation on its own.
    fun calculateDailyCalorieTarget() {
        viewModelScope.launch {
            val profile = userProfileRepository.getProfile() ?: return@launch
            applyCalorieGoal(profile)
        }
    }

    // Mifflin-St Jeor TDEE, then a calorie deficit/surplus toward the goal weight and deadline.
    // No-ops if a target was already calculated, so this never clobbers a manual edit.
    private suspend fun applyCalorieGoal(profile: UserProfile) {
        if (profile.dailyCalorieTarget > 0) {
            _uiState.update { it.copy(dailyCalorieTarget = profile.dailyCalorieTarget) }
            return
        }
        val bmr = CalorieCalculator.calculateBmr(
            weightKg = profile.weightKg,
            heightCm = profile.heightCm,
            age = profile.age,
            biologicalSex = profile.biologicalSex,
        )
        val tdee = CalorieCalculator.calculateTdee(bmr, profile.activityLevel)
        val target = CalorieCalculator.calculateDailyCalorieTarget(
            tdee = tdee,
            currentWeightKg = profile.weightKg,
            targetWeightKg = profile.targetWeightKg,
            goalDeadlineWeeks = profile.goalDeadlineWeeks,
        )
        userProfileRepository.updateDailyCalorieTarget(target)
        _uiState.update { it.copy(calculatedTdee = tdee.roundToInt(), dailyCalorieTarget = target) }
    }
}
