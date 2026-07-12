package com.nutritrack.app.di

import com.nutritrack.app.data.repository.FoodLookupRepository
import com.nutritrack.app.data.repository.OpenFoodFactsLookupRepository
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
}
