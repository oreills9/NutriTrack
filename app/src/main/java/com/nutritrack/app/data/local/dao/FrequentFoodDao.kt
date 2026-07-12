package com.nutritrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nutritrack.app.data.local.entity.FrequentFoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FrequentFoodDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(food: FrequentFoodEntity): Long

    @Update
    suspend fun update(food: FrequentFoodEntity)

    @Delete
    suspend fun delete(food: FrequentFoodEntity)

    @Query("SELECT * FROM frequent_food WHERE id = :id")
    suspend fun getById(id: Long): FrequentFoodEntity?

    @Query("SELECT * FROM frequent_food WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): FrequentFoodEntity?

    @Query("SELECT * FROM frequent_food ORDER BY log_count DESC LIMIT :limit")
    fun observeMostUsed(limit: Int = 20): Flow<List<FrequentFoodEntity>>

    @Query("SELECT * FROM frequent_food WHERE food_name LIKE '%' || :query || '%' ORDER BY log_count DESC")
    suspend fun searchByName(query: String): List<FrequentFoodEntity>

    @Query("UPDATE frequent_food SET log_count = log_count + 1 WHERE id = :id")
    suspend fun incrementLogCount(id: Long)
}
