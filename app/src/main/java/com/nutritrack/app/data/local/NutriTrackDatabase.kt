package com.nutritrack.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nutritrack.app.data.local.dao.ActivityEntryDao
import com.nutritrack.app.data.local.dao.BloodPressureEntryDao
import com.nutritrack.app.data.local.dao.DailyLogDao
import com.nutritrack.app.data.local.dao.FoodEntryDao
import com.nutritrack.app.data.local.dao.FrequentFoodDao
import com.nutritrack.app.data.local.dao.SupplementEntryDao
import com.nutritrack.app.data.local.dao.UserProfileDao
import com.nutritrack.app.data.local.dao.WeightEntryDao
import com.nutritrack.app.data.local.entity.ActivityEntryEntity
import com.nutritrack.app.data.local.entity.BloodPressureEntryEntity
import com.nutritrack.app.data.local.entity.DailyLogEntity
import com.nutritrack.app.data.local.entity.FoodEntryEntity
import com.nutritrack.app.data.local.entity.FrequentFoodEntity
import com.nutritrack.app.data.local.entity.SupplementEntryEntity
import com.nutritrack.app.data.local.entity.UserProfileEntity
import com.nutritrack.app.data.local.entity.WeightEntryEntity

@Database(
    entities = [
        UserProfileEntity::class,
        FoodEntryEntity::class,
        FrequentFoodEntity::class,
        ActivityEntryEntity::class,
        WeightEntryEntity::class,
        BloodPressureEntryEntity::class,
        SupplementEntryEntity::class,
        DailyLogEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class NutriTrackDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun frequentFoodDao(): FrequentFoodDao
    abstract fun activityEntryDao(): ActivityEntryDao
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun bloodPressureEntryDao(): BloodPressureEntryDao
    abstract fun supplementEntryDao(): SupplementEntryDao
    abstract fun dailyLogDao(): DailyLogDao

    companion object {
        const val DATABASE_NAME = "nutritrack.db"
    }
}
