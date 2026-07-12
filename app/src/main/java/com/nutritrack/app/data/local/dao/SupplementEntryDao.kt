package com.nutritrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nutritrack.app.data.local.entity.SupplementEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplementEntryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: SupplementEntryEntity): Long

    @Update
    suspend fun update(entry: SupplementEntryEntity)

    @Delete
    suspend fun delete(entry: SupplementEntryEntity)

    @Query("SELECT * FROM supplement_entry ORDER BY time_of_day ASC")
    fun observeAll(): Flow<List<SupplementEntryEntity>>

    @Query("UPDATE supplement_entry SET taken_today = :taken WHERE id = :id")
    suspend fun setTaken(id: Long, taken: Boolean)

    @Query("UPDATE supplement_entry SET taken_today = 0")
    suspend fun resetAllTakenStatus()
}
