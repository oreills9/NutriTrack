package com.nutritrack.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "frequent_food",
    indices = [Index(value = ["barcode"]), Index(value = ["food_name"])],
)
data class FrequentFoodEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "food_name") val foodName: String,
    @ColumnInfo(name = "barcode") val barcode: String? = null,
    @ColumnInfo(name = "average_quantity") val averageQuantity: Double,
    @ColumnInfo(name = "quantity_unit") val quantityUnit: QuantityUnit,
    @ColumnInfo(name = "calories_per_100g") val caloriesPer100g: Double,
    @ColumnInfo(name = "protein_per_100g") val proteinPer100g: Double,
    @ColumnInfo(name = "fat_per_100g") val fatPer100g: Double,
    @ColumnInfo(name = "carbs_per_100g") val carbsPer100g: Double,
    @ColumnInfo(name = "source") val source: DataSource,
    @ColumnInfo(name = "log_count") val logCount: Int = 1,
)
