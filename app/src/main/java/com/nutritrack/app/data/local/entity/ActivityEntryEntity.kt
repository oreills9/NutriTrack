package com.nutritrack.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "activity_entry",
    indices = [Index(value = ["date"])],
)
data class ActivityEntryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "activity_type") val activityType: ActivityType,
    @ColumnInfo(name = "duration_minutes") val durationMinutes: Int,
    @ColumnInfo(name = "intensity") val intensity: Intensity,
    @ColumnInfo(name = "met_value") val metValue: Double,
    @ColumnInfo(name = "calories_burned") val caloriesBurned: Double,
    @ColumnInfo(name = "timestamp") val timestamp: Instant,
)
