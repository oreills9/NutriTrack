package com.nutritrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nutritrack.app.data.local.entity.BloodPressureEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface BloodPressureEntryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: BloodPressureEntryEntity): Long

    @Update
    suspend fun update(entry: BloodPressureEntryEntity)

    @Delete
    suspend fun delete(entry: BloodPressureEntryEntity)

    @Query("SELECT * FROM blood_pressure_entry ORDER BY date DESC")
    fun observeAll(): Flow<List<BloodPressureEntryEntity>>

    @Query("SELECT * FROM blood_pressure_entry WHERE date = :date ORDER BY time_of_day ASC")
    fun observeForDate(date: LocalDate): Flow<List<BloodPressureEntryEntity>>

    @Query("SELECT * FROM blood_pressure_entry WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun observeBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<BloodPressureEntryEntity>>
}
