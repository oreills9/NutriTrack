package com.nutritrack.app.data.repository

import com.nutritrack.app.data.remote.openfoodfacts.OpenFoodFactsApi
import javax.inject.Inject
import javax.inject.Singleton

data class ScannedFood(
    val barcode: String,
    val name: String,
    val brand: String?,
    val caloriesPer100g: Double?,
    val proteinPer100g: Double?,
    val carbsPer100g: Double?,
    val fatPer100g: Double?,
    val servingSize: String?,
)

sealed interface FoodLookupResult {
    data class Found(val food: ScannedFood) : FoodLookupResult
    data object NotFound : FoodLookupResult
    data class Error(val throwable: Throwable) : FoodLookupResult
}

interface FoodLookupRepository {
    suspend fun lookupBarcode(barcode: String): FoodLookupResult
}

@Singleton
class OpenFoodFactsLookupRepository @Inject constructor(
    private val api: OpenFoodFactsApi,
) : FoodLookupRepository {

    override suspend fun lookupBarcode(barcode: String): FoodLookupResult = try {
        val response = api.getProduct(barcode)
        val product = response.product
        if (response.status != 1 || product == null) {
            FoodLookupResult.NotFound
        } else {
            FoodLookupResult.Found(
                ScannedFood(
                    barcode = barcode,
                    name = product.productName ?: "Unknown food",
                    brand = product.brands,
                    caloriesPer100g = product.nutriments?.energyKcalPer100g,
                    proteinPer100g = product.nutriments?.proteinsPer100g,
                    carbsPer100g = product.nutriments?.carbohydratesPer100g,
                    fatPer100g = product.nutriments?.fatPer100g,
                    servingSize = product.servingSize,
                ),
            )
        }
    } catch (t: Throwable) {
        FoodLookupResult.Error(t)
    }
}
