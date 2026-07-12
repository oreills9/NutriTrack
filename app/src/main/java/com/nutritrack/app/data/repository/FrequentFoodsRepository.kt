package com.nutritrack.app.data.repository

import com.nutritrack.app.data.local.dao.FrequentFoodDao
import com.nutritrack.app.data.local.entity.FrequentFoodEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface FrequentFoodsRepository {
    fun observeTopFrequentFoods(limit: Int = 10): Flow<List<FrequentFoodEntity>>
    suspend fun searchByName(query: String): List<FrequentFoodEntity>

    // Increments the existing entry's log count (matched by barcode, then name), or creates
    // a new one, so the top-frequent list stays accurate every time a food is logged.
    suspend fun recordLog(food: FrequentFoodEntity)
}

@Singleton
class RoomFrequentFoodsRepository @Inject constructor(
    private val dao: FrequentFoodDao,
) : FrequentFoodsRepository {

    override fun observeTopFrequentFoods(limit: Int): Flow<List<FrequentFoodEntity>> = dao.observeMostUsed(limit)

    override suspend fun searchByName(query: String): List<FrequentFoodEntity> = dao.searchByName(query)

    override suspend fun recordLog(food: FrequentFoodEntity) {
        val existing = food.barcode?.let { dao.getByBarcode(it) }
            ?: dao.searchByName(food.foodName).firstOrNull { it.foodName.equals(food.foodName, ignoreCase = true) }
        if (existing != null) {
            dao.incrementLogCount(existing.id)
        } else {
            dao.insert(food)
        }
    }
}
