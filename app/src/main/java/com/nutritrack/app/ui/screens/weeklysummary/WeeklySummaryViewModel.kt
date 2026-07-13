package com.nutritrack.app.ui.screens.weeklysummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.data.local.entity.ActivityEntryEntity
import com.nutritrack.app.data.local.entity.DailyLogEntity
import com.nutritrack.app.data.local.entity.FoodEntryEntity
import com.nutritrack.app.data.local.entity.MealSlot
import com.nutritrack.app.data.local.entity.WeightEntryEntity
import com.nutritrack.app.data.repository.ActivityLogRepository
import com.nutritrack.app.data.repository.DailyLogRepository
import com.nutritrack.app.data.repository.FoodDiaryRepository
import com.nutritrack.app.data.repository.UserProfile
import com.nutritrack.app.data.repository.UserProfileRepository
import com.nutritrack.app.data.repository.WeightHistoryRepository
import com.nutritrack.app.domain.weeklysummary.WeeklySummaryInsightGenerator
import com.nutritrack.app.domain.weight.WeightGoalCalculator
import com.nutritrack.app.domain.weight.WeightGoalProgress
import com.nutritrack.app.domain.weight.WeightTrendCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class DayChartPoint(
    val date: LocalDate,
    val intake: Double,
    val burned: Double,
)

data class MealSlotBreakdownItem(
    val mealSlot: MealSlot,
    val averageCalories: Double,
    val percentageOfIntake: Float,
)

data class WeeklySummaryUiState(
    val weekStart: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY),
    val chartPoints: List<DayChartPoint> = emptyList(),
    val dailyCalorieTarget: Int? = null,
    val averageDailyIntake: Double = 0.0,
    val averageActivityBurn: Double = 0.0,
    val weightChangeKg: Double? = null,
    val daysFullyLogged: Int = 0,
    val mealSlotBreakdown: List<MealSlotBreakdownItem> = emptyList(),
    val weightGoalProgress: WeightGoalProgress? = null,
    val insights: List<String> = emptyList(),
    val isLoading: Boolean = true,
) {
    val weekEnd: LocalDate get() = weekStart.plusDays(6)

    val canNavigateForward: Boolean
        get() = weekStart.isBefore(LocalDate.now().with(DayOfWeek.MONDAY))
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WeeklySummaryViewModel @Inject constructor(
    private val foodDiaryRepository: FoodDiaryRepository,
    private val activityLogRepository: ActivityLogRepository,
    private val weightHistoryRepository: WeightHistoryRepository,
    private val userProfileRepository: UserProfileRepository,
    private val dailyLogRepository: DailyLogRepository,
) : ViewModel() {

    private val weekStartFlow = MutableStateFlow(LocalDate.now().with(DayOfWeek.MONDAY))

    private val _uiState = MutableStateFlow(WeeklySummaryUiState())
    val uiState: StateFlow<WeeklySummaryUiState> = _uiState.asStateFlow()

    init {
        weekStartFlow.flatMapLatest { weekStart ->
            val weekEnd = weekStart.plusDays(6)
            combine(
                foodDiaryRepository.observeEntriesBetweenDates(weekStart, weekEnd),
                activityLogRepository.observeEntriesBetweenDates(weekStart, weekEnd),
                weightHistoryRepository.observeAllEntries(),
                userProfileRepository.observeProfile(),
            ) { foodEntries, activityEntries, allWeightEntries, profile ->
                buildUiState(weekStart, foodEntries, activityEntries, allWeightEntries, profile)
            }
        }.onEach { state ->
            _uiState.value = state
            reconcileDailyLogs(state.chartPoints)
        }.launchIn(viewModelScope)
    }

    fun goToPreviousWeek() {
        weekStartFlow.update { it.minusWeeks(1) }
    }

    fun goToNextWeek() {
        val next = weekStartFlow.value.plusWeeks(1)
        if (!next.isAfter(LocalDate.now().with(DayOfWeek.MONDAY))) {
            weekStartFlow.value = next
        }
    }

    private fun buildUiState(
        weekStart: LocalDate,
        foodEntries: List<FoodEntryEntity>,
        activityEntries: List<ActivityEntryEntity>,
        allWeightEntries: List<WeightEntryEntity>,
        profile: UserProfile?,
    ): WeeklySummaryUiState {
        val days = (0..6).map { weekStart.plusDays(it.toLong()) }
        val intakeByDay = days.map { day -> foodEntries.filter { it.date == day }.sumOf { it.calories } }
        val burnedByDay = days.map { day -> activityEntries.filter { it.date == day }.sumOf { it.caloriesBurned } }
        val chartPoints = days.mapIndexed { i, day -> DayChartPoint(day, intakeByDay[i], burnedByDay[i]) }

        val daysFullyLogged = days.count { day -> foodEntries.any { it.date == day } }
        val loggedDaysIntake = intakeByDay.filter { it > 0.0 }
        val averageDailyIntake = if (loggedDaysIntake.isNotEmpty()) loggedDaysIntake.average() else 0.0
        val loggedDaysBurn = burnedByDay.filter { it > 0.0 }
        val averageActivityBurn = if (loggedDaysBurn.isNotEmpty()) loggedDaysBurn.average() else 0.0

        // observeAllEntries() is ordered date DESC, which weightAsOf() requires.
        val weekEndWeight = WeightTrendCalculator.weightAsOf(allWeightEntries, weekStart.plusDays(6))
        val weekStartWeight = WeightTrendCalculator.weightAsOf(allWeightEntries, weekStart.minusDays(1))
        val weightChangeKg = if (weekEndWeight != null && weekStartWeight != null) {
            weekEndWeight - weekStartWeight
        } else {
            null
        }

        val dailyCalorieTarget = profile?.dailyCalorieTarget?.takeIf { it > 0 }

        val weightGoalProgress = profile?.let { p ->
            val startWeight = allWeightEntries.lastOrNull()?.weightKg ?: p.weightKg
            val currentWeight = allWeightEntries.firstOrNull()?.weightKg ?: p.weightKg
            val currentWeek = (ChronoUnit.WEEKS.between(p.dateCreated, LocalDate.now()) + 1).toInt()
            WeightGoalCalculator.calculateProgress(
                startWeightKg = startWeight,
                currentWeightKg = currentWeight,
                targetWeightKg = p.targetWeightKg,
                currentWeek = currentWeek,
                totalWeeks = p.goalDeadlineWeeks,
            )
        }

        val insights = WeeklySummaryInsightGenerator.generate(
            weekStart = weekStart,
            dailyIntake = intakeByDay,
            dailyBurned = burnedByDay,
            dailyCalorieTarget = dailyCalorieTarget,
            daysFullyLogged = daysFullyLogged,
            weightChangeKg = weightChangeKg,
        )

        return WeeklySummaryUiState(
            weekStart = weekStart,
            chartPoints = chartPoints,
            dailyCalorieTarget = dailyCalorieTarget,
            averageDailyIntake = averageDailyIntake,
            averageActivityBurn = averageActivityBurn,
            weightChangeKg = weightChangeKg,
            daysFullyLogged = daysFullyLogged,
            mealSlotBreakdown = buildMealSlotBreakdown(foodEntries),
            weightGoalProgress = weightGoalProgress,
            insights = insights,
            isLoading = false,
        )
    }

    private fun buildMealSlotBreakdown(foodEntries: List<FoodEntryEntity>): List<MealSlotBreakdownItem> {
        val totalIntake = foodEntries.sumOf { it.calories }
        return MealSlot.entries.map { slot ->
            val slotTotal = foodEntries.filter { it.mealSlot == slot }.sumOf { it.calories }
            MealSlotBreakdownItem(
                mealSlot = slot,
                averageCalories = slotTotal / 7.0,
                percentageOfIntake = if (totalIntake > 0.0) (slotTotal / totalIntake * 100).toFloat() else 0f,
            )
        }
    }

    // Keeps DailyLog in sync with what Diary/Activity Log already show, since that table is
    // otherwise only written to from the Activity Log screen.
    private fun reconcileDailyLogs(chartPoints: List<DayChartPoint>) {
        viewModelScope.launch {
            val target = userProfileRepository.getProfile()?.dailyCalorieTarget?.takeIf { it > 0 }
            chartPoints.forEach { point ->
                if (point.intake <= 0.0 && point.burned <= 0.0) return@forEach
                val net = point.intake - point.burned
                dailyLogRepository.upsert(
                    DailyLogEntity(
                        date = point.date,
                        totalCaloriesConsumed = point.intake,
                        totalActivityCaloriesBurned = point.burned,
                        netCalories = net,
                        isComplete = point.intake > 0.0,
                        targetHit = target != null && net <= target,
                    ),
                )
            }
        }
    }
}
