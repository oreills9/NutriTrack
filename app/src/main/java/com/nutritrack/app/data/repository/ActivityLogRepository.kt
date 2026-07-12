package com.nutritrack.app.data.repository

import com.nutritrack.app.data.local.dao.ActivityEntryDao
import com.nutritrack.app.data.local.entity.ActivityEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

interface ActivityLogRepository {
    fun observeEntriesForDate(date: LocalDate): Flow<List<ActivityEntryEntity>>
    fun observeTotalCaloriesBurnedForDate(date: LocalDate): Flow<Double>
    suspend fun logActivity(entry: ActivityEntryEntity): Long
    suspend fun updateEntry(entry: ActivityEntryEntity)
    suspend fun deleteEntry(entry: ActivityEntryEntity)
}

@Singleton
class RoomActivityLogRepository @Inject constructor(
    private val dao: ActivityEntryDao,
) : ActivityLogRepository {

    override fun observeEntriesForDate(date: LocalDate): Flow<List<ActivityEntryEntity>> = dao.observeForDate(date)

    override fun observeTotalCaloriesBurnedForDate(date: LocalDate): Flow<Double> =
        dao.observeTotalCaloriesBurnedForDate(date)

    override suspend fun logActivity(entry: ActivityEntryEntity): Long = dao.insert(entry)

    override suspend fun updateEntry(entry: ActivityEntryEntity) = dao.update(entry)

    override suspend fun deleteEntry(entry: ActivityEntryEntity) = dao.delete(entry)
}
