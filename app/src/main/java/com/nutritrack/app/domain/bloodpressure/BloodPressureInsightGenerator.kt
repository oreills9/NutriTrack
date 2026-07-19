package com.nutritrack.app.domain.bloodpressure

import com.nutritrack.app.data.local.entity.BloodPressureEntryEntity
import com.nutritrack.app.data.local.entity.TimeOfDay
import kotlin.math.abs
import kotlin.math.roundToInt

object BloodPressureInsightGenerator {

    // Expects readings oldest-first (as returned by BloodPressureAnalyzer.filterForRange).
    fun generate(readings: List<BloodPressureEntryEntity>): List<String> {
        val insights = mutableListOf<String>()
        trendInsight(readings)?.let { insights += it }
        highReadingInsight(readings)?.let { insights += it }
        normalRangeInsight(readings)?.let { insights += it }
        timeOfDayInsight(readings)?.let { insights += it }
        return insights.take(3)
    }

    private fun trendInsight(readings: List<BloodPressureEntryEntity>): String? {
        if (readings.size < 4) return null
        val midpoint = readings.size / 2
        val firstHalfAvg = readings.subList(0, midpoint).map { it.systolic }.average()
        val secondHalfAvg = readings.subList(midpoint, readings.size).map { it.systolic }.average()
        val delta = secondHalfAvg - firstHalfAvg
        return when {
            delta <= -3.0 -> "Your systolic average has trended down by ${(-delta).roundToInt()} mmHg over this period."
            delta >= 3.0 -> "Your systolic average has trended up by ${delta.roundToInt()} mmHg over this period - worth keeping an eye on."
            else -> "Your blood pressure has stayed fairly stable over this period."
        }
    }

    private fun highReadingInsight(readings: List<BloodPressureEntryEntity>): String? {
        val highCount = readings.count { BloodPressureAnalyzer.classify(it.systolic, it.diastolic) == BloodPressureCategory.HIGH_STAGE_2 }
        if (highCount == 0) return null
        val plural = if (highCount == 1) "reading was" else "readings were"
        return "$highCount $plural in the High Stage 2 range - consider discussing this with your doctor."
    }

    private fun normalRangeInsight(readings: List<BloodPressureEntryEntity>): String? {
        if (readings.isEmpty()) return null
        val normalCount = readings.count { BloodPressureAnalyzer.classify(it.systolic, it.diastolic) == BloodPressureCategory.NORMAL }
        val percentage = (normalCount * 100.0 / readings.size).roundToInt()
        if (percentage < 50) return null
        return "$percentage% of your readings this period were in the Normal range."
    }

    private fun timeOfDayInsight(readings: List<BloodPressureEntryEntity>): String? {
        val morningAvg = readings.filter { it.timeOfDay == TimeOfDay.MORNING }.map { it.systolic }
        val eveningAvg = readings.filter { it.timeOfDay == TimeOfDay.EVENING }.map { it.systolic }
        if (morningAvg.isEmpty() || eveningAvg.isEmpty()) return null
        val delta = eveningAvg.average() - morningAvg.average()
        if (abs(delta) < 5.0) return null
        return if (delta > 0) {
            "Your evening readings run ${delta.roundToInt()} mmHg higher on average than your morning readings."
        } else {
            "Your morning readings run ${(-delta).roundToInt()} mmHg higher on average than your evening readings."
        }
    }
}
