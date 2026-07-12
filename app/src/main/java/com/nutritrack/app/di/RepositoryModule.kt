package com.nutritrack.app.di

import com.nutritrack.app.data.repository.FoodLookupRepository
import com.nutritrack.app.data.repository.OpenFoodFactsLookupRepository
import com.nutritrack.app.data.repository.RoomUserProfileRepository
import com.nutritrack.app.data.repository.UserProfileRepository
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
}
