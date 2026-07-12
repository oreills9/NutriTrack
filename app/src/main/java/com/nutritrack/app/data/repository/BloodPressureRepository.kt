package com.nutritrack.app.data.repository

import com.nutritrack.app.data.local.dao.BloodPressureEntryDao
import com.nutritrack.app.data.local.entity.BloodPressureEntryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface BloodPressureRepository {
    fun observeAllReadings(): Flow<List<BloodPressureEntryEntity>>
    fun observeLatestReading(): Flow<BloodPressureEntryEntity?>
    suspend fun logReading(entry: BloodPressureEntryEntity): Long
    suspend fun updateReading(entry: BloodPressureEntryEntity)
    suspend fun deleteReading(entry: BloodPressureEntryEntity)
}

@Singleton
class RoomBloodPressureRepository @Inject constructor(
    private val dao: BloodPressureEntryDao,
) : BloodPressureRepository {

    override fun observeAllReadings(): Flow<List<BloodPressureEntryEntity>> = dao.observeAll()

    override fun observeLatestReading(): Flow<BloodPressureEntryEntity?> = dao.observeLatest()

    override suspend fun logReading(entry: BloodPressureEntryEntity): Long = dao.insert(entry)

    override suspend fun updateReading(entry: BloodPressureEntryEntity) = dao.update(entry)

    override suspend fun deleteReading(entry: BloodPressureEntryEntity) = dao.delete(entry)
}
