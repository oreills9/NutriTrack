package com.nutritrack.app.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import com.nutritrack.app.BuildConfig
import com.nutritrack.app.data.local.entity.ActivityLevel
import com.nutritrack.app.data.local.entity.BiologicalSex
import com.nutritrack.app.data.local.entity.UnitSystem
import com.nutritrack.app.data.prefs.AppPreferencesRepository
import com.nutritrack.app.data.repository.DataManagementRepository
import com.nutritrack.app.data.repository.UserProfile
import com.nutritrack.app.data.repository.UserProfileRepository
import com.nutritrack.app.domain.calorie.CalorieCalculator
import com.nutritrack.app.notifications.BloodPressureReminderScheduler
import com.nutritrack.app.notifications.MealGapReminderScheduler
import com.nutritrack.app.notifications.SupplementReminderScheduler
import com.nutritrack.app.notifications.WeighInReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = true,
    val preferredUnits: UnitSystem = UnitSystem.METRIC,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val age: Int? = null,
    val biologicalSex: BiologicalSex? = null,
    val activityLevel: ActivityLevel? = null,
    val targetWeightKg: Double? = null,
    val goalDeadlineWeeks: Int? = null,
    val dailyCalorieTarget: Int? = null,
    val isSavingProfile: Boolean = false,
    val profileSavedMessage: String? = null,

    val nutritionixAppId: String = "",
    val nutritionixAppKey: String = "",
    val isSavingNutritionixKeys: Boolean = false,
    val nutritionixSavedMessage: String? = null,

    val mealGapReminderEnabled: Boolean = false,
    val mealGapReminderHours: Int = 4,
    val sundayBpReminderEnabled: Boolean = true,
    val sundayBpReminderTime: LocalTime = LocalTime.of(9, 0),
    val sundayWeighInReminderEnabled: Boolean = false,
    val sundayWeighInReminderTime: LocalTime = LocalTime.of(9, 0),
    val supplementReminderEnabled: Boolean = false,
    val supplementReminderTime: LocalTime = LocalTime.of(9, 0),

    val isExporting: Boolean = false,
    val exportMessage: String? = null,
    val isResetting: Boolean = false,
    val resetComplete: Boolean = false,

    val appVersion: String = "",
) {
    val isPersonalDetailsValid: Boolean
        get() = heightCm != null && weightKg != null && age != null && biologicalSex != null && activityLevel != null

    val isGoalSettingsValid: Boolean
        get() = targetWeightKg != null && goalDeadlineWeeks != null
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userProfileRepository: UserProfileRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val dataManagementRepository: DataManagementRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // One-time hydrate of the editable form fields, so a live re-sync doesn't clobber
        // in-progress edits (same reasoning as ProfileSetup/AddFood).
        viewModelScope.launch {
            userProfileRepository.getProfile()?.let { profile ->
                _uiState.update {
                    it.copy(
                        preferredUnits = profile.preferredUnits,
                        heightCm = profile.heightCm,
                        weightKg = profile.weightKg,
                        age = profile.age,
                        biologicalSex = profile.biologicalSex,
                        activityLevel = profile.activityLevel,
                        targetWeightKg = profile.targetWeightKg,
                        goalDeadlineWeeks = profile.goalDeadlineWeeks,
                        dailyCalorieTarget = profile.dailyCalorieTarget.takeIf { target -> target > 0 },
                    )
                }
            }
            _uiState.update {
                it.copy(
                    nutritionixAppId = appPreferencesRepository.nutritionixAppId.first(),
                    nutritionixAppKey = appPreferencesRepository.nutritionixAppKey.first(),
                    isLoading = false,
                )
            }
        }

        // Notification prefs take effect immediately on toggle, so these stay live/reactive.
        combine(
            appPreferencesRepository.mealGapReminderEnabled,
            appPreferencesRepository.mealGapReminderHours,
            appPreferencesRepository.sundayBpReminderEnabled,
            appPreferencesRepository.sundayBpReminderTime,
        ) { mealGapEnabled, mealGapHours, bpEnabled, bpTime ->
            _uiState.update {
                it.copy(
                    mealGapReminderEnabled = mealGapEnabled,
                    mealGapReminderHours = mealGapHours,
                    sundayBpReminderEnabled = bpEnabled,
                    sundayBpReminderTime = bpTime,
                )
            }
        }.launchIn(viewModelScope)

        combine(
            appPreferencesRepository.sundayWeighInReminderEnabled,
            appPreferencesRepository.sundayWeighInReminderTime,
            appPreferencesRepository.supplementReminderEnabled,
            appPreferencesRepository.supplementReminderTime,
        ) { weighInEnabled, weighInTime, supplementEnabled, supplementTime ->
            _uiState.update {
                it.copy(
                    sundayWeighInReminderEnabled = weighInEnabled,
                    sundayWeighInReminderTime = weighInTime,
                    supplementReminderEnabled = supplementEnabled,
                    supplementReminderTime = supplementTime,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun updatePreferredUnits(value: UnitSystem) = _uiState.update { it.copy(preferredUnits = value) }
    fun updateHeightCm(value: Double?) = _uiState.update { it.copy(heightCm = value) }
    fun updateWeightKg(value: Double?) = _uiState.update { it.copy(weightKg = value) }
    fun updateAge(value: Int?) = _uiState.update { it.copy(age = value) }
    fun updateBiologicalSex(value: BiologicalSex) = _uiState.update { it.copy(biologicalSex = value) }
    fun updateActivityLevel(value: ActivityLevel) = _uiState.update { it.copy(activityLevel = value) }
    fun updateTargetWeightKg(value: Double?) = _uiState.update { it.copy(targetWeightKg = value) }
    fun updateGoalDeadlineWeeks(value: Int?) = _uiState.update { it.copy(goalDeadlineWeeks = value) }

    // Recalculates the daily calorie target every time (unlike ProfileSetup's onboarding guard) -
    // this is an explicit edit flow, so the inputs driving the calculation just changed on purpose.
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
            _uiState.update { it.copy(isSavingProfile = true) }
            val existing = userProfileRepository.getProfile()
            val bmr = CalorieCalculator.calculateBmr(weightKg, heightCm, age, biologicalSex)
            val tdee = CalorieCalculator.calculateTdee(bmr, activityLevel)
            val newTarget = CalorieCalculator.calculateDailyCalorieTarget(tdee, weightKg, targetWeightKg, goalDeadlineWeeks)

            userProfileRepository.saveProfile(
                UserProfile(
                    heightCm = heightCm,
                    weightKg = weightKg,
                    age = age,
                    biologicalSex = biologicalSex,
                    activityLevel = activityLevel,
                    preferredUnits = state.preferredUnits,
                    dailyCalorieTarget = newTarget,
                    targetWeightKg = targetWeightKg,
                    goalDeadlineWeeks = goalDeadlineWeeks,
                    dateCreated = existing?.dateCreated ?: LocalDate.now(),
                ),
            )
            _uiState.update {
                it.copy(
                    isSavingProfile = false,
                    dailyCalorieTarget = newTarget,
                    profileSavedMessage = "Saved - daily target updated to $newTarget kcal",
                )
            }
        }
    }

    fun updateNutritionixAppId(value: String) = _uiState.update { it.copy(nutritionixAppId = value) }
    fun updateNutritionixAppKey(value: String) = _uiState.update { it.copy(nutritionixAppKey = value) }

    fun saveNutritionixKeys() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingNutritionixKeys = true) }
            appPreferencesRepository.setNutritionixAppId(state.nutritionixAppId.trim())
            appPreferencesRepository.setNutritionixAppKey(state.nutritionixAppKey.trim())
            _uiState.update { it.copy(isSavingNutritionixKeys = false, nutritionixSavedMessage = "Saved") }
        }
    }

    fun setMealGapReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.setMealGapReminderEnabled(enabled)
            if (enabled) {
                MealGapReminderScheduler.schedule(
                    context,
                    _uiState.value.mealGapReminderHours,
                    ExistingPeriodicWorkPolicy.REPLACE,
                )
            } else {
                MealGapReminderScheduler.cancel(context)
            }
        }
    }

    fun setMealGapReminderHours(hours: Int) {
        viewModelScope.launch {
            appPreferencesRepository.setMealGapReminderHours(hours)
            if (_uiState.value.mealGapReminderEnabled) {
                MealGapReminderScheduler.schedule(context, hours, ExistingPeriodicWorkPolicy.REPLACE)
            }
        }
    }

    fun setSundayBpReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.setSundayBpReminderEnabled(enabled)
            if (enabled) {
                BloodPressureReminderScheduler.schedule(
                    context,
                    _uiState.value.sundayBpReminderTime,
                    ExistingPeriodicWorkPolicy.REPLACE,
                )
            } else {
                BloodPressureReminderScheduler.cancel(context)
            }
        }
    }

    fun setSundayBpReminderTime(time: LocalTime) {
        viewModelScope.launch {
            appPreferencesRepository.setSundayBpReminderTime(time)
            if (_uiState.value.sundayBpReminderEnabled) {
                BloodPressureReminderScheduler.schedule(context, time, ExistingPeriodicWorkPolicy.REPLACE)
            }
        }
    }

    fun setSundayWeighInReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.setSundayWeighInReminderEnabled(enabled)
            if (enabled) {
                WeighInReminderScheduler.schedule(
                    context,
                    _uiState.value.sundayWeighInReminderTime,
                    ExistingPeriodicWorkPolicy.REPLACE,
                )
            } else {
                WeighInReminderScheduler.cancel(context)
            }
        }
    }

    fun setSundayWeighInReminderTime(time: LocalTime) {
        viewModelScope.launch {
            appPreferencesRepository.setSundayWeighInReminderTime(time)
            if (_uiState.value.sundayWeighInReminderEnabled) {
                WeighInReminderScheduler.schedule(context, time, ExistingPeriodicWorkPolicy.REPLACE)
            }
        }
    }

    fun setSupplementReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.setSupplementReminderEnabled(enabled)
            if (enabled) {
                SupplementReminderScheduler.schedule(
                    context,
                    _uiState.value.supplementReminderTime,
                    ExistingPeriodicWorkPolicy.REPLACE,
                )
            } else {
                SupplementReminderScheduler.cancel(context)
            }
        }
    }

    fun setSupplementReminderTime(time: LocalTime) {
        viewModelScope.launch {
            appPreferencesRepository.setSupplementReminderTime(time)
            if (_uiState.value.supplementReminderEnabled) {
                SupplementReminderScheduler.schedule(context, time, ExistingPeriodicWorkPolicy.REPLACE)
            }
        }
    }

    fun exportAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val uri = dataManagementRepository.exportAllDataAsCsv()
            _uiState.update {
                it.copy(isExporting = false, exportMessage = if (uri != null) "Exported to Downloads" else "Export failed")
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isResetting = true) }
            BloodPressureReminderScheduler.cancel(context)
            WeighInReminderScheduler.cancel(context)
            SupplementReminderScheduler.cancel(context)
            MealGapReminderScheduler.cancel(context)
            dataManagementRepository.resetAllData()
            _uiState.update { it.copy(isResetting = false, resetComplete = true) }
        }
    }

    fun clearProfileSavedMessage() = _uiState.update { it.copy(profileSavedMessage = null) }
    fun clearNutritionixSavedMessage() = _uiState.update { it.copy(nutritionixSavedMessage = null) }
    fun clearExportMessage() = _uiState.update { it.copy(exportMessage = null) }
}
