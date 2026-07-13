package com.nutritrack.app.domain.weeklysummary

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

object WeeklySummaryInsightGenerator {

    // dailyIntake/dailyBurned are 7 values, index 0 = weekStart (Monday) .. index 6 = Sunday.
    fun generate(
        weekStart: LocalDate,
        dailyIntake: List<Double>,
        dailyBurned: List<Double>,
        dailyCalorieTarget: Int?,
        daysFullyLogged: Int,
        weightChangeKg: Double?,
    ): List<String> {
        val insights = mutableListOf<String>()
        targetHitInsight(dailyIntake, dailyBurned, dailyCalorieTarget)?.let { insights += it }
        mostActiveDayInsight(weekStart, dailyBurned)?.let { insights += it }
        weightChangeInsight(weightChangeKg)?.let { insights += it }
        loggingConsistencyInsight(daysFullyLogged, dailyIntake.size)?.let { insights += it }
        return insights.take(3)
    }

    private fun targetHitInsight(intake: List<Double>, burned: List<Double>, target: Int?): String? {
        if (target == null || target <= 0) return null
        val daysLogged = intake.indices.count { intake[it] > 0.0 }
        if (daysLogged == 0) return null
        val daysHit = intake.indices.count { i -> intake[i] > 0.0 && (intake[i] - burned[i]) <= target }
        return "You hit your calorie target on $daysHit of $daysLogged logged days this week."
    }

    private fun mostActiveDayInsight(weekStart: LocalDate, burned: List<Double>): String? {
        val maxIndex = burned.indices.maxByOrNull { burned[it] } ?: return null
        if (burned[maxIndex] <= 0.0) return null
        val dayName = weekStart.plusDays(maxIndex.toLong())
            .dayOfWeek
            .getDisplayName(TextStyle.FULL, Locale.getDefault())
        return "$dayName was your most active day, burning ${burned[maxIndex].roundToInt()} kcal."
    }

    private fun weightChangeInsight(weightChangeKg: Double?): String? {
        if (weightChangeKg == null) return null
        return when {
            weightChangeKg <= -0.1 -> "You lost ${"%.1f".format(-weightChangeKg)} kg this week."
            weightChangeKg >= 0.1 -> "You gained ${"%.1f".format(weightChangeKg)} kg this week."
            else -> "Your weight held steady this week."
        }
    }

    private fun loggingConsistencyInsight(daysFullyLogged: Int, totalDays: Int): String? {
        if (totalDays == 0) return null
        return when {
            daysFullyLogged >= totalDays - 1 ->
                "Great consistency - you logged food on $daysFullyLogged of $totalDays days."
            daysFullyLogged <= totalDays / 2 ->
                "You only logged food on $daysFullyLogged of $totalDays days - try to log daily for more accurate trends."
            else -> null
        }
    }
}
