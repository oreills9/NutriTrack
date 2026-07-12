package com.nutritrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nutritrack.app.data.local.entity.FoodEntryEntity
import com.nutritrack.app.data.local.entity.MealSlot
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface FoodEntryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: FoodEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entries: List<FoodEntryEntity>): List<Long>

    @Update
    suspend fun update(entry: FoodEntryEntity)

    @Delete
    suspend fun delete(entry: FoodEntryEntity)

    @Query("DELETE FROM food_entry WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM food_entry WHERE id = :id")
    suspend fun getById(id: Long): FoodEntryEntity?

    @Query("SELECT * FROM food_entry WHERE date = :date ORDER BY timestamp ASC")
    fun observeForDate(date: LocalDate): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entry WHERE date = :date AND meal_slot = :mealSlot ORDER BY timestamp ASC")
    fun observeForDateAndMealSlot(date: LocalDate, mealSlot: MealSlot): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entry WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, timestamp ASC")
    fun observeBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<FoodEntryEntity>>

    @Query("SELECT COALESCE(SUM(calories), 0.0) FROM food_entry WHERE date = :date")
    fun observeTotalCaloriesForDate(date: LocalDate): Flow<Double>
}
