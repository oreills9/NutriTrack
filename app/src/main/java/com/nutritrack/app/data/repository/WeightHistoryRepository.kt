package com.nutritrack.app.data.repository

import com.nutritrack.app.data.local.dao.WeightEntryDao
import com.nutritrack.app.data.local.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface WeightHistoryRepository {
    fun observeAllEntries(): Flow<List<WeightEntryEntity>>
    suspend fun logWeight(entry: WeightEntryEntity): Long
    suspend fun updateEntry(entry: WeightEntryEntity)
    suspend fun deleteEntry(entry: WeightEntryEntity)
}

@Singleton
class RoomWeightHistoryRepository @Inject constructor(
    private val dao: WeightEntryDao,
) : WeightHistoryRepository {

    override fun observeAllEntries(): Flow<List<WeightEntryEntity>> = dao.observeAll()

    override suspend fun logWeight(entry: WeightEntryEntity): Long = dao.insert(entry)

    override suspend fun updateEntry(entry: WeightEntryEntity) = dao.update(entry)

    override suspend fun deleteEntry(entry: WeightEntryEntity) = dao.delete(entry)
}
