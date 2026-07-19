package com.nutritrack.app.data.repository

import com.nutritrack.app.data.local.dao.FoodEntryDao
import com.nutritrack.app.data.local.entity.FoodEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

interface FoodDiaryRepository {
    fun observeEntriesForDate(date: LocalDate): Flow<List<FoodEntryEntity>>
    fun observeEntriesBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<FoodEntryEntity>>
    fun observeTotalCaloriesForDate(date: LocalDate): Flow<Double>
    suspend fun getAllEntries(): List<FoodEntryEntity>
    suspend fun getMostRecentEntry(): FoodEntryEntity?
    suspend fun logFood(entry: FoodEntryEntity): Long
    suspend fun updateEntry(entry: FoodEntryEntity)
    suspend fun deleteEntry(entry: FoodEntryEntity)
}

@Singleton
class RoomFoodDiaryRepository @Inject constructor(
    private val dao: FoodEntryDao,
) : FoodDiaryRepository {

    override fun observeEntriesForDate(date: LocalDate): Flow<List<FoodEntryEntity>> = dao.observeForDate(date)

    override fun observeEntriesBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<FoodEntryEntity>> =
        dao.observeBetweenDates(startDate, endDate)

    override fun observeTotalCaloriesForDate(date: LocalDate): Flow<Double> = dao.observeTotalCaloriesForDate(date)

    override suspend fun getAllEntries(): List<FoodEntryEntity> = dao.getAll()

    override suspend fun getMostRecentEntry(): FoodEntryEntity? = dao.getMostRecent()

    override suspend fun logFood(entry: FoodEntryEntity): Long = dao.insert(entry)

    override suspend fun updateEntry(entry: FoodEntryEntity) = dao.update(entry)

    override suspend fun deleteEntry(entry: FoodEntryEntity) = dao.delete(entry)
}
