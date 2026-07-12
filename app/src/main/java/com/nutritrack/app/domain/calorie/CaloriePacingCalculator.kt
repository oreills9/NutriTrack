package com.nutritrack.app.domain.calorie

import java.time.LocalTime

enum class CaloriePaceIndicator { UNDER_PACE, ON_PACE, OVER_PACE }

object CaloriePacingCalculator {

    private const val SECONDS_PER_DAY = 86_400.0
    private const val PACE_TOLERANCE_FRACTION = 0.1

    fun calculatePaceIndicator(
        caloriesConsumed: Double,
        dailyCalorieTarget: Int,
        now: LocalTime = LocalTime.now(),
    ): CaloriePaceIndicator {
        val fractionOfDayElapsed = now.toSecondOfDay() / SECONDS_PER_DAY
        val expectedCaloriesByNow = dailyCalorieTarget * fractionOfDayElapsed
        val tolerance = dailyCalorieTarget * PACE_TOLERANCE_FRACTION
        return when {
            caloriesConsumed > expectedCaloriesByNow + tolerance -> CaloriePaceIndicator.OVER_PACE
            caloriesConsumed < expectedCaloriesByNow - tolerance -> CaloriePaceIndicator.UNDER_PACE
            else -> CaloriePaceIndicator.ON_PACE
        }
    }
}
