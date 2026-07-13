package com.nutritrack.app.domain.weight

data class WeightGoalProgress(
    val startWeightKg: Double,
    val currentWeightKg: Double,
    val targetWeightKg: Double,
    val currentWeek: Int,
    val totalWeeks: Int,
    val progressFraction: Float,
)

object WeightGoalCalculator {

    fun calculateProgress(
        startWeightKg: Double,
        currentWeightKg: Double,
        targetWeightKg: Double,
        currentWeek: Int,
        totalWeeks: Int,
    ): WeightGoalProgress {
        val totalDelta = targetWeightKg - startWeightKg
        val currentDelta = currentWeightKg - startWeightKg
        val fraction = if (totalDelta == 0.0) 1f else (currentDelta / totalDelta).toFloat().coerceIn(0f, 1f)
        return WeightGoalProgress(
            startWeightKg = startWeightKg,
            currentWeightKg = currentWeightKg,
            targetWeightKg = targetWeightKg,
            currentWeek = currentWeek.coerceIn(1, totalWeeks.coerceAtLeast(1)),
            totalWeeks = totalWeeks,
            progressFraction = fraction,
        )
    }
}
