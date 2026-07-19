package com.nutritrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nutritrack.app.data.local.entity.DailyLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: DailyLogEntity)

    @Update
    suspend fun update(log: DailyLogEntity)

    @Delete
    suspend fun delete(log: DailyLogEntity)

    @Query("SELECT * FROM daily_log WHERE date = :date")
    suspend fun getForDate(date: LocalDate): DailyLogEntity?

    @Query("SELECT * FROM daily_log WHERE date = :date")
    fun observeForDate(date: LocalDate): Flow<DailyLogEntity?>

    @Query("SELECT * FROM daily_log WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun observeBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_log ORDER BY date ASC")
    suspend fun getAll(): List<DailyLogEntity>
}
