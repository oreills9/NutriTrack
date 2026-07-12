package com.nutritrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nutritrack.app.data.local.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WeightEntryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: WeightEntryEntity): Long

    @Update
    suspend fun update(entry: WeightEntryEntity)

    @Delete
    suspend fun delete(entry: WeightEntryEntity)

    @Query("SELECT * FROM weight_entry ORDER BY date DESC")
    fun observeAll(): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entry ORDER BY date DESC LIMIT 1")
    fun observeLatest(): Flow<WeightEntryEntity?>

    @Query("SELECT * FROM weight_entry WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun observeBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<WeightEntryEntity>>
}
