package com.nutritrack.app.data.repository

import com.nutritrack.app.data.local.dao.SupplementEntryDao
import com.nutritrack.app.data.local.entity.SupplementEntryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface SupplementsRepository {
    fun observeChecklist(): Flow<List<SupplementEntryEntity>>
    suspend fun getById(id: Long): SupplementEntryEntity?
    suspend fun addSupplement(entry: SupplementEntryEntity): Long
    suspend fun updateSupplement(entry: SupplementEntryEntity)
    suspend fun deleteSupplement(entry: SupplementEntryEntity)
    suspend fun setTaken(id: Long, taken: Boolean)
    suspend fun resetDailyChecklist()
}

@Singleton
class RoomSupplementsRepository @Inject constructor(
    private val dao: SupplementEntryDao,
) : SupplementsRepository {

    override fun observeChecklist(): Flow<List<SupplementEntryEntity>> = dao.observeAll()

    override suspend fun getById(id: Long): SupplementEntryEntity? = dao.getById(id)

    override suspend fun addSupplement(entry: SupplementEntryEntity): Long = dao.insert(entry)

    override suspend fun updateSupplement(entry: SupplementEntryEntity) = dao.update(entry)

    override suspend fun deleteSupplement(entry: SupplementEntryEntity) = dao.delete(entry)

    override suspend fun setTaken(id: Long, taken: Boolean) = dao.setTaken(id, taken)

    override suspend fun resetDailyChecklist() = dao.resetAllTakenStatus()
}
