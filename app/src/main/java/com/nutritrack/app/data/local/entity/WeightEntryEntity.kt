package com.nutritrack.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "weight_entry",
    indices = [Index(value = ["date"])],
)
data class WeightEntryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "weight_kg") val weightKg: Double,
    @ColumnInfo(name = "note") val note: String? = null,
)
