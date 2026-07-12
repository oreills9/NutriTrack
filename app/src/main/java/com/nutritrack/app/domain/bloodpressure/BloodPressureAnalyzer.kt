package com.nutritrack.app.domain.bloodpressure

import com.nutritrack.app.data.local.entity.BloodPressureEntryEntity
import java.time.LocalDate

enum class BloodPressureCategory { NORMAL, ELEVATED, HIGH_STAGE_1, HIGH_STAGE_2 }

data class BloodPressureAverages(
    val averageSystolic: Double,
    val averageDiastolic: Double,
    val averageHeartRateBpm: Double,
)

object BloodPressureAnalyzer {

    private const val AVERAGE_WINDOW_DAYS = 56L // 8 weeks

    // AHA guidelines: higher-severity thresholds checked first since either value alone can bump the category.
    fun classify(systolic: Int, diastolic: Int): BloodPressureCategory = when {
        systolic >= 140 || diastolic >= 90 -> BloodPressureCategory.HIGH_STAGE_2
        systolic >= 130 || diastolic >= 80 -> BloodPressureCategory.HIGH_STAGE_1
        systolic >= 120 -> BloodPressureCategory.ELEVATED
        else -> BloodPressureCategory.NORMAL
    }

    fun calculateAverages(
        readings: List<BloodPressureEntryEntity>,
        today: LocalDate = LocalDate.now(),
    ): BloodPressureAverages? {
        val windowStart = today.minusDays(AVERAGE_WINDOW_DAYS - 1)
        val recent = readings.filter { it.date >= windowStart && it.date <= today }
        if (recent.isEmpty()) return null
        return BloodPressureAverages(
            averageSystolic = recent.map { it.systolic }.average(),
            averageDiastolic = recent.map { it.diastolic }.average(),
            averageHeartRateBpm = recent.map { it.heartRateBpm }.average(),
        )
    }
}
