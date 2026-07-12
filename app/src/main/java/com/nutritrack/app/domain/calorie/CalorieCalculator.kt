package com.nutritrack.app.domain.calorie

import com.nutritrack.app.data.local.entity.ActivityLevel
import com.nutritrack.app.data.local.entity.BiologicalSex
import kotlin.math.roundToInt

object CalorieCalculator {

    private const val KCAL_PER_KG_BODY_FAT = 7700.0
    private const val MIN_SAFE_DAILY_CALORIES = 1200

    fun calculateBmr(weightKg: Double, heightCm: Double, age: Int, biologicalSex: BiologicalSex): Double {
        val base = 10 * weightKg + 6.25 * heightCm - 5 * age
        return when (biologicalSex) {
            BiologicalSex.MALE -> base + 5
            BiologicalSex.FEMALE -> base - 161
            // Mifflin-St Jeor only defines male/female constants; split the difference as a neutral fallback.
            BiologicalSex.OTHER -> base - 78
        }
    }

    fun calculateTdee(bmr: Double, activityLevel: ActivityLevel): Double = bmr * activityLevel.calorieMultiplier()

    // Clamped to MIN_SAFE_DAILY_CALORIES so an aggressive deadline can't suggest a crash diet.
    fun calculateDailyCalorieTarget(
        tdee: Double,
        currentWeightKg: Double,
        targetWeightKg: Double,
        goalDeadlineWeeks: Int,
    ): Int {
        if (goalDeadlineWeeks <= 0) return tdee.roundToInt().coerceAtLeast(MIN_SAFE_DAILY_CALORIES)
        val weightChangeKg = currentWeightKg - targetWeightKg
        val totalCalorieAdjustment = weightChangeKg * KCAL_PER_KG_BODY_FAT
        val dailyAdjustment = totalCalorieAdjustment / (goalDeadlineWeeks * 7)
        return (tdee - dailyAdjustment).roundToInt().coerceAtLeast(MIN_SAFE_DAILY_CALORIES)
    }

    private fun ActivityLevel.calorieMultiplier(): Double = when (this) {
        ActivityLevel.SEDENTARY -> 1.2
        ActivityLevel.LIGHTLY_ACTIVE -> 1.375
        ActivityLevel.MODERATELY_ACTIVE -> 1.55
        ActivityLevel.VERY_ACTIVE -> 1.725
        ActivityLevel.EXTRA_ACTIVE -> 1.9
    }
}
