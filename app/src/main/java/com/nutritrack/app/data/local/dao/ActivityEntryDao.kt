package com.nutritrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nutritrack.app.data.local.entity.ActivityEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ActivityEntryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: ActivityEntryEntity): Long

    @Update
    suspend fun update(entry: ActivityEntryEntity)

    @Delete
    suspend fun delete(entry: ActivityEntryEntity)

    @Query("DELETE FROM activity_entry WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM activity_entry WHERE id = :id")
    suspend fun getById(id: Long): ActivityEntryEntity?

    @Query("SELECT * FROM activity_entry WHERE date = :date ORDER BY timestamp ASC")
    fun observeForDate(date: LocalDate): Flow<List<ActivityEntryEntity>>

    @Query("SELECT * FROM activity_entry WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, timestamp ASC")
    fun observeBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<ActivityEntryEntity>>

    @Query("SELECT COALESCE(SUM(calories_burned), 0.0) FROM activity_entry WHERE date = :date")
    fun observeTotalCaloriesBurnedForDate(date: LocalDate): Flow<Double>
}
