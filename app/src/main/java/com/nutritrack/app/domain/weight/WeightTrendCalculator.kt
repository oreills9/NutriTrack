package com.nutritrack.app.domain.weight

import com.nutritrack.app.data.local.entity.WeightEntryEntity

data class WeightTrendPoint(
    val entry: WeightEntryEntity,
    val rollingAverageKg: Double,
)

object WeightTrendCalculator {

    private const val ROLLING_WINDOW_DAYS = 21L

    // entriesAscending must be sorted oldest-to-newest by date.
    fun calculateRollingAverages(entriesAscending: List<WeightEntryEntity>): List<WeightTrendPoint> =
        entriesAscending.map { entry ->
            val windowStart = entry.date.minusDays(ROLLING_WINDOW_DAYS - 1)
            val windowEntries = entriesAscending.filter { it.date >= windowStart && it.date <= entry.date }
            WeightTrendPoint(
                entry = entry,
                rollingAverageKg = windowEntries.map { it.weightKg }.average(),
            )
        }

    fun calculateTotalWeightLost(entriesAscending: List<WeightEntryEntity>): Double {
        val first = entriesAscending.firstOrNull() ?: return 0.0
        val last = entriesAscending.lastOrNull() ?: return 0.0
        return first.weightKg - last.weightKg
    }
}
