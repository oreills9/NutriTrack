package com.nutritrack.app.di

import android.content.Context
import androidx.room.Room
import com.nutritrack.app.data.local.NutriTrackDatabase
import com.nutritrack.app.data.local.dao.ActivityEntryDao
import com.nutritrack.app.data.local.dao.BloodPressureEntryDao
import com.nutritrack.app.data.local.dao.DailyLogDao
import com.nutritrack.app.data.local.dao.FoodEntryDao
import com.nutritrack.app.data.local.dao.FrequentFoodDao
import com.nutritrack.app.data.local.dao.SupplementEntryDao
import com.nutritrack.app.data.local.dao.UserProfileDao
import com.nutritrack.app.data.local.dao.WeightEntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNutriTrackDatabase(@ApplicationContext context: Context): NutriTrackDatabase =
        Room.databaseBuilder(
            context,
            NutriTrackDatabase::class.java,
            NutriTrackDatabase.DATABASE_NAME,
        ).build()

    @Provides
    fun provideUserProfileDao(database: NutriTrackDatabase): UserProfileDao = database.userProfileDao()

    @Provides
    fun provideFoodEntryDao(database: NutriTrackDatabase): FoodEntryDao = database.foodEntryDao()

    @Provides
    fun provideFrequentFoodDao(database: NutriTrackDatabase): FrequentFoodDao = database.frequentFoodDao()

    @Provides
    fun provideActivityEntryDao(database: NutriTrackDatabase): ActivityEntryDao = database.activityEntryDao()

    @Provides
    fun provideWeightEntryDao(database: NutriTrackDatabase): WeightEntryDao = database.weightEntryDao()

    @Provides
    fun provideBloodPressureEntryDao(database: NutriTrackDatabase): BloodPressureEntryDao =
        database.bloodPressureEntryDao()

    @Provides
    fun provideSupplementEntryDao(database: NutriTrackDatabase): SupplementEntryDao =
        database.supplementEntryDao()

    @Provides
    fun provideDailyLogDao(database: NutriTrackDatabase): DailyLogDao = database.dailyLogDao()
}
