package com.nutritrack.app.widget

import com.nutritrack.app.data.repository.ActivityLogRepository
import com.nutritrack.app.data.repository.FoodDiaryRepository
import com.nutritrack.app.data.repository.UserProfileRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// GlanceAppWidget instances aren't created through Hilt's usual injection points, so
// provideGlance() pulls its dependencies out of the Application's Hilt graph via this entry point.
@EntryPoint
@InstallIn(SingletonComponent::class)
interface NutriWidgetEntryPoint {
    fun foodDiaryRepository(): FoodDiaryRepository
    fun activityLogRepository(): ActivityLogRepository
    fun userProfileRepository(): UserProfileRepository
}
