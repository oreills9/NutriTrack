package com.nutritrack.app.data.repository

import com.nutritrack.app.data.local.dao.UserProfileDao
import com.nutritrack.app.data.local.entity.ActivityLevel
import com.nutritrack.app.data.local.entity.BiologicalSex
import com.nutritrack.app.data.local.entity.UnitSystem
import com.nutritrack.app.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class UserProfile(
    val heightCm: Double,
    val weightKg: Double,
    val age: Int,
    val biologicalSex: BiologicalSex,
    val activityLevel: ActivityLevel,
    val preferredUnits: UnitSystem,
    val dailyCalorieTarget: Int,
    val targetWeightKg: Double,
    val goalDeadlineWeeks: Int,
    val dateCreated: LocalDate,
)

interface UserProfileRepository {
    fun observeProfile(): Flow<UserProfile?>
    suspend fun getProfile(): UserProfile?
    suspend fun saveProfile(profile: UserProfile)
    suspend fun updateDailyCalorieTarget(target: Int)
    suspend fun updateCurrentWeight(weightKg: Double)
}

@Singleton
class RoomUserProfileRepository @Inject constructor(
    private val dao: UserProfileDao,
) : UserProfileRepository {

    override fun observeProfile(): Flow<UserProfile?> = dao.observe().map { it?.toDomain() }

    override suspend fun getProfile(): UserProfile? = dao.get()?.toDomain()

    override suspend fun saveProfile(profile: UserProfile) = dao.upsert(profile.toEntity())

    override suspend fun updateDailyCalorieTarget(target: Int) = dao.updateDailyCalorieTarget(target)

    override suspend fun updateCurrentWeight(weightKg: Double) = dao.updateWeight(weightKg)
}

private fun UserProfileEntity.toDomain() = UserProfile(
    heightCm = heightCm,
    weightKg = weightKg,
    age = age,
    biologicalSex = biologicalSex,
    activityLevel = activityLevel,
    preferredUnits = preferredUnits,
    dailyCalorieTarget = dailyCalorieTarget,
    targetWeightKg = targetWeightKg,
    goalDeadlineWeeks = goalDeadlineWeeks,
    dateCreated = dateCreated,
)

private fun UserProfile.toEntity() = UserProfileEntity(
    heightCm = heightCm,
    weightKg = weightKg,
    age = age,
    biologicalSex = biologicalSex,
    activityLevel = activityLevel,
    preferredUnits = preferredUnits,
    dailyCalorieTarget = dailyCalorieTarget,
    targetWeightKg = targetWeightKg,
    goalDeadlineWeeks = goalDeadlineWeeks,
    dateCreated = dateCreated,
)
