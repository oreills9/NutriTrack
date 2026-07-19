package com.nutritrack.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")
private val DEFAULT_REMINDER_TIME: LocalTime = LocalTime.of(9, 0)

interface AppPreferencesRepository {
    val hasCompletedOnboarding: Flow<Boolean>
    suspend fun setOnboardingComplete()
    suspend fun clearOnboardingComplete()

    val nutritionixAppId: Flow<String>
    suspend fun setNutritionixAppId(value: String)

    val nutritionixAppKey: Flow<String>
    suspend fun setNutritionixAppKey(value: String)

    val mealGapReminderEnabled: Flow<Boolean>
    suspend fun setMealGapReminderEnabled(enabled: Boolean)
    val mealGapReminderHours: Flow<Int>
    suspend fun setMealGapReminderHours(hours: Int)

    val sundayBpReminderEnabled: Flow<Boolean>
    suspend fun setSundayBpReminderEnabled(enabled: Boolean)
    val sundayBpReminderTime: Flow<LocalTime>
    suspend fun setSundayBpReminderTime(time: LocalTime)

    val sundayWeighInReminderEnabled: Flow<Boolean>
    suspend fun setSundayWeighInReminderEnabled(enabled: Boolean)
    val sundayWeighInReminderTime: Flow<LocalTime>
    suspend fun setSundayWeighInReminderTime(time: LocalTime)

    val supplementReminderEnabled: Flow<Boolean>
    suspend fun setSupplementReminderEnabled(enabled: Boolean)
    val supplementReminderTime: Flow<LocalTime>
    suspend fun setSupplementReminderTime(time: LocalTime)
}

@Singleton
class DataStoreAppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppPreferencesRepository {

    private object Keys {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val NUTRITIONIX_APP_ID = stringPreferencesKey("nutritionix_app_id")
        val NUTRITIONIX_APP_KEY = stringPreferencesKey("nutritionix_app_key")
        val MEAL_GAP_REMINDER_ENABLED = booleanPreferencesKey("meal_gap_reminder_enabled")
        val MEAL_GAP_REMINDER_HOURS = intPreferencesKey("meal_gap_reminder_hours")
        val SUNDAY_BP_REMINDER_ENABLED = booleanPreferencesKey("sunday_bp_reminder_enabled")
        val SUNDAY_BP_REMINDER_TIME_SECONDS = intPreferencesKey("sunday_bp_reminder_time_seconds")
        val SUNDAY_WEIGH_IN_REMINDER_ENABLED = booleanPreferencesKey("sunday_weigh_in_reminder_enabled")
        val SUNDAY_WEIGH_IN_REMINDER_TIME_SECONDS = intPreferencesKey("sunday_weigh_in_reminder_time_seconds")
        val SUPPLEMENT_REMINDER_ENABLED = booleanPreferencesKey("supplement_reminder_enabled")
        val SUPPLEMENT_REMINDER_TIME_SECONDS = intPreferencesKey("supplement_reminder_time_seconds")
    }

    override val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Keys.ONBOARDING_COMPLETE] ?: false }

    override suspend fun setOnboardingComplete() {
        context.dataStore.edit { preferences -> preferences[Keys.ONBOARDING_COMPLETE] = true }
    }

    override suspend fun clearOnboardingComplete() {
        context.dataStore.edit { preferences -> preferences[Keys.ONBOARDING_COMPLETE] = false }
    }

    override val nutritionixAppId: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[Keys.NUTRITIONIX_APP_ID] ?: "" }

    override suspend fun setNutritionixAppId(value: String) {
        context.dataStore.edit { preferences -> preferences[Keys.NUTRITIONIX_APP_ID] = value }
    }

    override val nutritionixAppKey: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[Keys.NUTRITIONIX_APP_KEY] ?: "" }

    override suspend fun setNutritionixAppKey(value: String) {
        context.dataStore.edit { preferences -> preferences[Keys.NUTRITIONIX_APP_KEY] = value }
    }

    override val mealGapReminderEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Keys.MEAL_GAP_REMINDER_ENABLED] ?: false }

    override suspend fun setMealGapReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[Keys.MEAL_GAP_REMINDER_ENABLED] = enabled }
    }

    override val mealGapReminderHours: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[Keys.MEAL_GAP_REMINDER_HOURS] ?: 4 }

    override suspend fun setMealGapReminderHours(hours: Int) {
        context.dataStore.edit { preferences -> preferences[Keys.MEAL_GAP_REMINDER_HOURS] = hours }
    }

    override val sundayBpReminderEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Keys.SUNDAY_BP_REMINDER_ENABLED] ?: true }

    override suspend fun setSundayBpReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[Keys.SUNDAY_BP_REMINDER_ENABLED] = enabled }
    }

    override val sundayBpReminderTime: Flow<LocalTime> = context.dataStore.data
        .map { preferences -> preferences[Keys.SUNDAY_BP_REMINDER_TIME_SECONDS].toLocalTime() }

    override suspend fun setSundayBpReminderTime(time: LocalTime) {
        context.dataStore.edit { preferences -> preferences[Keys.SUNDAY_BP_REMINDER_TIME_SECONDS] = time.toSecondOfDay() }
    }

    override val sundayWeighInReminderEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Keys.SUNDAY_WEIGH_IN_REMINDER_ENABLED] ?: false }

    override suspend fun setSundayWeighInReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[Keys.SUNDAY_WEIGH_IN_REMINDER_ENABLED] = enabled }
    }

    override val sundayWeighInReminderTime: Flow<LocalTime> = context.dataStore.data
        .map { preferences -> preferences[Keys.SUNDAY_WEIGH_IN_REMINDER_TIME_SECONDS].toLocalTime() }

    override suspend fun setSundayWeighInReminderTime(time: LocalTime) {
        context.dataStore.edit {
            preferences -> preferences[Keys.SUNDAY_WEIGH_IN_REMINDER_TIME_SECONDS] = time.toSecondOfDay()
        }
    }

    override val supplementReminderEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Keys.SUPPLEMENT_REMINDER_ENABLED] ?: false }

    override suspend fun setSupplementReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[Keys.SUPPLEMENT_REMINDER_ENABLED] = enabled }
    }

    override val supplementReminderTime: Flow<LocalTime> = context.dataStore.data
        .map { preferences -> preferences[Keys.SUPPLEMENT_REMINDER_TIME_SECONDS].toLocalTime() }

    override suspend fun setSupplementReminderTime(time: LocalTime) {
        context.dataStore.edit { preferences -> preferences[Keys.SUPPLEMENT_REMINDER_TIME_SECONDS] = time.toSecondOfDay() }
    }

    private fun Int?.toLocalTime(): LocalTime = this?.let { LocalTime.ofSecondOfDay(it.toLong()) } ?: DEFAULT_REMINDER_TIME
}
