package com.nutritrack.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_log")
data class DailyLogEntity(
    @PrimaryKey
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "total_calories_consumed") val totalCaloriesConsumed: Double,
    @ColumnInfo(name = "total_activity_calories_burned") val totalActivityCaloriesBurned: Double,
    @ColumnInfo(name = "net_calories") val netCalories: Double,
    @ColumnInfo(name = "is_complete") val isComplete: Boolean = false,
    @ColumnInfo(name = "target_hit") val targetHit: Boolean = false,
)
