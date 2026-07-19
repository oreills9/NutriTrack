package com.nutritrack.app.domain.bloodpressure

import com.nutritrack.app.data.local.entity.BloodPressureEntryEntity
import java.time.format.DateTimeFormatter

object BloodPressureCsvExporter {

    private val DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE

    fun toCsv(readings: List<BloodPressureEntryEntity>): String {
        val builder = StringBuilder()
        builder.appendLine("date,systolic,diastolic,heartRateBpm,timeOfDay,category,note")
        readings.sortedBy { it.date }.forEach { reading ->
            val category = BloodPressureAnalyzer.classify(reading.systolic, reading.diastolic)
            val row = listOf(
                reading.date.format(DATE_FORMAT),
                reading.systolic,
                reading.diastolic,
                reading.heartRateBpm,
                reading.timeOfDay,
                category,
                reading.note.orEmpty(),
            )
            builder.appendLine(row.joinToString(",") { it.toString().toCsvField() })
        }
        return builder.toString()
    }

    private fun String.toCsvField(): String =
        if (contains(",") || contains("\"") || contains("\n")) "\"${replace("\"", "\"\"")}\"" else this
}
