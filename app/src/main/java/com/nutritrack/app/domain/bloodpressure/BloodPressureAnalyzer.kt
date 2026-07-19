package com.nutritrack.app.domain.bloodpressure

import com.nutritrack.app.data.local.entity.BloodPressureEntryEntity
import java.time.LocalDate

enum class BloodPressureCategory { NORMAL, ELEVATED, HIGH_STAGE_1, HIGH_STAGE_2 }

enum class BloodPressureTimeRange { FOUR_WEEKS, EIGHT_WEEKS, THREE_MONTHS, ALL_TIME }

data class BloodPressureAverages(
    val averageSystolic: Double,
    val averageDiastolic: Double,
    val averageHeartRateBpm: Double,
)

object BloodPressureAnalyzer {

    // AHA guidelines: higher-severity thresholds checked first since either value alone can bump the category.
    fun classify(systolic: Int, diastolic: Int): BloodPressureCategory = when {
        systolic >= 140 || diastolic >= 90 -> BloodPressureCategory.HIGH_STAGE_2
        systolic >= 130 || diastolic >= 80 -> BloodPressureCategory.HIGH_STAGE_1
        systolic >= 120 -> BloodPressureCategory.ELEVATED
        else -> BloodPressureCategory.NORMAL
    }

    // Returns readings within the range, oldest first (chart/insight friendly). ALL_TIME applies no window.
    fun filterForRange(
        readings: List<BloodPressureEntryEntity>,
        range: BloodPressureTimeRange,
        today: LocalDate = LocalDate.now(),
    ): List<BloodPressureEntryEntity> {
        val windowDays = when (range) {
            BloodPressureTimeRange.FOUR_WEEKS -> 28L
            BloodPressureTimeRange.EIGHT_WEEKS -> 56L
            BloodPressureTimeRange.THREE_MONTHS -> 90L
            BloodPressureTimeRange.ALL_TIME -> null
        }
        val inWindow = if (windowDays == null) {
            readings
        } else {
            val windowStart = today.minusDays(windowDays - 1)
            readings.filter { it.date >= windowStart && it.date <= today }
        }
        return inWindow.sortedBy { it.date }
    }

    fun calculateAverages(readings: List<BloodPressureEntryEntity>): BloodPressureAverages? {
        if (readings.isEmpty()) return null
        return BloodPressureAverages(
            averageSystolic = readings.map { it.systolic }.average(),
            averageDiastolic = readings.map { it.diastolic }.average(),
            averageHeartRateBpm = readings.map { it.heartRateBpm }.average(),
        )
    }
}
