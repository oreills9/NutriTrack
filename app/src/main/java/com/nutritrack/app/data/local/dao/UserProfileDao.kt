package com.nutritrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nutritrack.app.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfileEntity)

    @Update
    suspend fun update(profile: UserProfileEntity)

    @Delete
    suspend fun delete(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observe(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun get(): UserProfileEntity?

    @Query("UPDATE user_profile SET daily_calorie_target = :target WHERE id = 1")
    suspend fun updateDailyCalorieTarget(target: Int)

    @Query("UPDATE user_profile SET weight_kg = :weightKg WHERE id = 1")
    suspend fun updateWeight(weightKg: Double)
}
