package com.nutritrack.app.data.remote.openfoodfacts

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {
    @GET("api/v2/product/{barcode}.json")
    suspend fun getProduct(@Path("barcode") barcode: String): ProductResponse
}

@Serializable
data class ProductResponse(
    @SerialName("status") val status: Int,
    @SerialName("product") val product: ProductDto? = null,
)

@Serializable
data class ProductDto(
    @SerialName("product_name") val productName: String? = null,
    @SerialName("brands") val brands: String? = null,
    @SerialName("nutriments") val nutriments: NutrimentsDto? = null,
    @SerialName("serving_size") val servingSize: String? = null,
)

@Serializable
data class NutrimentsDto(
    @SerialName("energy-kcal_100g") val energyKcalPer100g: Double? = null,
    @SerialName("proteins_100g") val proteinsPer100g: Double? = null,
    @SerialName("carbohydrates_100g") val carbohydratesPer100g: Double? = null,
    @SerialName("fat_100g") val fatPer100g: Double? = null,
)
