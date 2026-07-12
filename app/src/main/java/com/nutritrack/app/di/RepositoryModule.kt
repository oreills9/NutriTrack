package com.nutritrack.app.di

import com.nutritrack.app.data.repository.ActivityLogRepository
import com.nutritrack.app.data.repository.BloodPressureRepository
import com.nutritrack.app.data.repository.DailyLogRepository
import com.nutritrack.app.data.repository.FoodDiaryRepository
import com.nutritrack.app.data.repository.FoodLookupRepository
import com.nutritrack.app.data.repository.FoodSearchRepository
import com.nutritrack.app.data.repository.FrequentFoodsRepository
import com.nutritrack.app.data.repository.OpenFoodFactsLookupRepository
import com.nutritrack.app.data.repository.RemoteFoodSearchRepository
import com.nutritrack.app.data.repository.RoomActivityLogRepository
import com.nutritrack.app.data.repository.RoomBloodPressureRepository
import com.nutritrack.app.data.repository.RoomDailyLogRepository
import com.nutritrack.app.data.repository.RoomFoodDiaryRepository
import com.nutritrack.app.data.repository.RoomFrequentFoodsRepository
import com.nutritrack.app.data.repository.RoomSupplementsRepository
import com.nutritrack.app.data.repository.RoomUserProfileRepository
import com.nutritrack.app.data.repository.RoomWeightHistoryRepository
import com.nutritrack.app.data.repository.SupplementsRepository
import com.nutritrack.app.data.repository.UserProfileRepository
import com.nutritrack.app.data.repository.WeightHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFoodLookupRepository(
        impl: OpenFoodFactsLookupRepository,
    ): FoodLookupRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        impl: RoomUserProfileRepository,
    ): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindFoodDiaryRepository(
        impl: RoomFoodDiaryRepository,
    ): FoodDiaryRepository

    @Binds
    @Singleton
    abstract fun bindActivityLogRepository(
        impl: RoomActivityLogRepository,
    ): ActivityLogRepository

    @Binds
    @Singleton
    abstract fun bindWeightHistoryRepository(
        impl: RoomWeightHistoryRepository,
    ): WeightHistoryRepository

    @Binds
    @Singleton
    abstract fun bindBloodPressureRepository(
        impl: RoomBloodPressureRepository,
    ): BloodPressureRepository

    @Binds
    @Singleton
    abstract fun bindFrequentFoodsRepository(
        impl: RoomFrequentFoodsRepository,
    ): FrequentFoodsRepository

    @Binds
    @Singleton
    abstract fun bindSupplementsRepository(
        impl: RoomSupplementsRepository,
    ): SupplementsRepository

    @Binds
    @Singleton
    abstract fun bindFoodSearchRepository(
        impl: RemoteFoodSearchRepository,
    ): FoodSearchRepository

    @Binds
    @Singleton
    abstract fun bindDailyLogRepository(
        impl: RoomDailyLogRepository,
    ): DailyLogRepository
}
