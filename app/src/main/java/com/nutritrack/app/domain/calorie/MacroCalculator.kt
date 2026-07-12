package com.nutritrack.app.domain.calorie

import kotlin.math.roundToInt

data class MacroSplit(
    val proteinGrams: Int,
    val fatGrams: Int,
    val carbGrams: Int,
)

object MacroCalculator {

    // A balanced, general-population split rather than a sport-specific one.
    private const val PROTEIN_CALORIE_FRACTION = 0.30
    private const val FAT_CALORIE_FRACTION = 0.30
    private const val CARB_CALORIE_FRACTION = 0.40

    private const val PROTEIN_KCAL_PER_GRAM = 4.0
    private const val FAT_KCAL_PER_GRAM = 9.0
    private const val CARB_KCAL_PER_GRAM = 4.0

    fun calculateMacroSplit(dailyCalorieTarget: Int): MacroSplit = MacroSplit(
        proteinGrams = (dailyCalorieTarget * PROTEIN_CALORIE_FRACTION / PROTEIN_KCAL_PER_GRAM).roundToInt(),
        fatGrams = (dailyCalorieTarget * FAT_CALORIE_FRACTION / FAT_KCAL_PER_GRAM).roundToInt(),
        carbGrams = (dailyCalorieTarget * CARB_CALORIE_FRACTION / CARB_KCAL_PER_GRAM).roundToInt(),
    )
}
