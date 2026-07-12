package com.nutritrack.app.data.repository

import com.nutritrack.app.data.local.dao.DailyLogDao
import com.nutritrack.app.data.local.entity.DailyLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

interface DailyLogRepository {
    fun observeForDate(date: LocalDate): Flow<DailyLogEntity?>
    suspend fun getForDate(date: LocalDate): DailyLogEntity?
    suspend fun upsert(log: DailyLogEntity)
}

@Singleton
class RoomDailyLogRepository @Inject constructor(
    private val dao: DailyLogDao,
) : DailyLogRepository {

    override fun observeForDate(date: LocalDate): Flow<DailyLogEntity?> = dao.observeForDate(date)

    override suspend fun getForDate(date: LocalDate): DailyLogEntity? = dao.getForDate(date)

    override suspend fun upsert(log: DailyLogEntity) = dao.upsert(log)
}
