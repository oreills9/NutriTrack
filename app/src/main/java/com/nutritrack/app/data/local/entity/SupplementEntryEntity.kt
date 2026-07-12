package com.nutritrack.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "supplement_entry")
data class SupplementEntryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "dosage_notes") val dosageNotes: String? = null,
    @ColumnInfo(name = "time_of_day") val timeOfDay: LocalTime,
    @ColumnInfo(name = "taken_today") val takenToday: Boolean = false,
)
