package com.nutritrack.app.di

import com.nutritrack.app.data.prefs.AppPreferencesRepository
import com.nutritrack.app.data.prefs.DataStoreAppPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {

    @Binds
    @Singleton
    abstract fun bindAppPreferencesRepository(
        impl: DataStoreAppPreferencesRepository,
    ): AppPreferencesRepository
}
