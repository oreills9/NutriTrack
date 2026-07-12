package com.nutritrack.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = SINGLE_ROW_ID,
    @ColumnInfo(name = "height_cm") val heightCm: Double,
    @ColumnInfo(name = "weight_kg") val weightKg: Double,
    @ColumnInfo(name = "age") val age: Int,
    @ColumnInfo(name = "biological_sex") val biologicalSex: BiologicalSex,
    @ColumnInfo(name = "activity_level") val activityLevel: ActivityLevel,
    @ColumnInfo(name = "preferred_units") val preferredUnits: UnitSystem,
    @ColumnInfo(name = "daily_calorie_target") val dailyCalorieTarget: Int,
    @ColumnInfo(name = "target_weight_kg") val targetWeightKg: Double,
    @ColumnInfo(name = "goal_deadline_weeks") val goalDeadlineWeeks: Int,
    @ColumnInfo(name = "date_created") val dateCreated: LocalDate,
) {
    companion object {
        const val SINGLE_ROW_ID = 1
    }
}
