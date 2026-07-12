package com.nutritrack.app.data.repository

import com.nutritrack.app.BuildConfig
import com.nutritrack.app.data.local.entity.DataSource
import com.nutritrack.app.data.remote.nutritionix.NutritionixApi
import com.nutritrack.app.data.remote.nutritionix.NutritionixFoodDto
import com.nutritrack.app.data.remote.nutritionix.NutritionixRequest
import com.nutritrack.app.data.remote.openfoodfacts.OpenFoodFactsApi
import com.nutritrack.app.data.remote.openfoodfacts.SearchProductDto
import javax.inject.Inject
import javax.inject.Singleton

data class FoodSearchResult(
    val barcode: String?,
    val name: String,
    val brand: String?,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double,
    val source: DataSource,
)

interface FoodSearchRepository {
    suspend fun searchOpenFoodFacts(query: String): List<FoodSearchResult>
    suspend fun searchNutritionix(query: String): List<FoodSearchResult>
}

@Singleton
class RemoteFoodSearchRepository @Inject constructor(
    private val openFoodFactsApi: OpenFoodFactsApi,
    private val nutritionixApi: NutritionixApi,
) : FoodSearchRepository {

    override suspend fun searchOpenFoodFacts(query: String): List<FoodSearchResult> = try {
        openFoodFactsApi.searchProducts(searchTerms = query).products
            .filter { !it.productName.isNullOrBlank() }
            .take(5)
            .map { it.toSearchResult() }
    } catch (t: Throwable) {
        emptyList()
    }

    // Nutritionix credentials are optional (see local.properties); without them this is a no-op
    // rather than a failed network call against blank headers.
    override suspend fun searchNutritionix(query: String): List<FoodSearchResult> {
        if (BuildConfig.NUTRITIONIX_APP_ID.isBlank() || BuildConfig.NUTRITIONIX_APP_KEY.isBlank()) {
            return emptyList()
        }
        return try {
            nutritionixApi.getNutrients(
                appId = BuildConfig.NUTRITIONIX_APP_ID,
                appKey = BuildConfig.NUTRITIONIX_APP_KEY,
                request = NutritionixRequest(query = query),
            ).foods.take(5).map { it.toSearchResult() }
        } catch (t: Throwable) {
            emptyList()
        }
    }
}

private fun SearchProductDto.toSearchResult() = FoodSearchResult(
    barcode = code,
    name = productName ?: "Unknown food",
    brand = brands,
    caloriesPer100g = nutriments?.energyKcalPer100g ?: 0.0,
    proteinPer100g = nutriments?.proteinsPer100g ?: 0.0,
    fatPer100g = nutriments?.fatPer100g ?: 0.0,
    carbsPer100g = nutriments?.carbohydratesPer100g ?: 0.0,
    source = DataSource.OPEN_FOOD_FACTS,
)

// Nutritionix returns absolute nutrients for the parsed serving, not per 100g, so this
// normalizes using the serving weight it also reports.
private fun NutritionixFoodDto.toSearchResult(): FoodSearchResult {
    val servingGrams = servingWeightGrams?.takeIf { it > 0 } ?: 100.0
    val per100gFactor = 100.0 / servingGrams
    return FoodSearchResult(
        barcode = null,
        name = foodName,
        brand = null,
        caloriesPer100g = (calories ?: 0.0) * per100gFactor,
        proteinPer100g = (proteinG ?: 0.0) * per100gFactor,
        fatPer100g = (fatG ?: 0.0) * per100gFactor,
        carbsPer100g = (carbsG ?: 0.0) * per100gFactor,
        source = DataSource.NUTRITIONIX,
    )
}
