package com.nutritrack.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "food_entry",
    indices = [Index(value = ["date"]), Index(value = ["barcode"])],
)
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "meal_slot") val mealSlot: MealSlot,
    @ColumnInfo(name = "food_name") val foodName: String,
    @ColumnInfo(name = "barcode") val barcode: String? = null,
    @ColumnInfo(name = "quantity") val quantity: Double,
    @ColumnInfo(name = "quantity_unit") val quantityUnit: QuantityUnit,
    @ColumnInfo(name = "calories") val calories: Double,
    @ColumnInfo(name = "protein_g") val proteinG: Double,
    @ColumnInfo(name = "fat_g") val fatG: Double,
    @ColumnInfo(name = "carbs_g") val carbsG: Double,
    @ColumnInfo(name = "source") val source: DataSource,
    @ColumnInfo(name = "timestamp") val timestamp: Instant,
)
