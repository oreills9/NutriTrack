package com.nutritrack.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

interface AppPreferencesRepository {
    val hasCompletedOnboarding: Flow<Boolean>
    suspend fun setOnboardingComplete()
}

@Singleton
class DataStoreAppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppPreferencesRepository {

    private object Keys {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    override val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Keys.ONBOARDING_COMPLETE] ?: false }

    override suspend fun setOnboardingComplete() {
        context.dataStore.edit { preferences -> preferences[Keys.ONBOARDING_COMPLETE] = true }
    }
}
