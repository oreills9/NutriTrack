package com.nutritrack.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "blood_pressure_entry",
    indices = [Index(value = ["date"])],
)
data class BloodPressureEntryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "systolic") val systolic: Int,
    @ColumnInfo(name = "diastolic") val diastolic: Int,
    @ColumnInfo(name = "heart_rate_bpm") val heartRateBpm: Int,
    @ColumnInfo(name = "time_of_day") val timeOfDay: TimeOfDay,
    @ColumnInfo(name = "note") val note: String? = null,
)
