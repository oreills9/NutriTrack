package com.nutritrack.app.domain.activity

object ActivityCalorieCalculator {

    fun calculateCaloriesBurned(metValue: Double, weightKg: Double, durationMinutes: Int): Double =
        metValue * weightKg * (durationMinutes / 60.0)
}
