package com.nutritrack.app.data.remote.nutritionix

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface NutritionixApi {
    @Headers("Content-Type: application/json")
    @POST("v2/natural/nutrients")
    suspend fun getNutrients(
        @Header("x-app-id") appId: String,
        @Header("x-app-key") appKey: String,
        @Body request: NutritionixRequest,
    ): NutritionixResponse
}

@Serializable
data class NutritionixRequest(
    @SerialName("query") val query: String,
)

@Serializable
data class NutritionixResponse(
    @SerialName("foods") val foods: List<NutritionixFoodDto> = emptyList(),
)

@Serializable
data class NutritionixFoodDto(
    @SerialName("food_name") val foodName: String,
    @SerialName("nf_calories") val calories: Double? = null,
    @SerialName("nf_protein") val proteinG: Double? = null,
    @SerialName("nf_total_fat") val fatG: Double? = null,
    @SerialName("nf_total_carbohydrate") val carbsG: Double? = null,
    @SerialName("serving_weight_grams") val servingWeightGrams: Double? = null,
)
