package com.nutritrack.app.ui.screens.addfood

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.data.local.entity.DataSource
import com.nutritrack.app.data.local.entity.FoodEntryEntity
import com.nutritrack.app.data.local.entity.FrequentFoodEntity
import com.nutritrack.app.data.local.entity.MealSlot
import com.nutritrack.app.data.local.entity.QuantityUnit
import com.nutritrack.app.data.repository.FoodDiaryRepository
import com.nutritrack.app.data.repository.FoodLookupRepository
import com.nutritrack.app.data.repository.FoodLookupResult
import com.nutritrack.app.data.repository.FoodSearchRepository
import com.nutritrack.app.data.repository.FoodSearchResult
import com.nutritrack.app.data.repository.FrequentFoodsRepository
import com.nutritrack.app.data.repository.ScannedFood
import com.nutritrack.app.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

enum class AddFoodTab { SEARCH, SCAN }

data class SelectedFood(
    val name: String,
    val barcode: String?,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double,
    val source: DataSource,
)

data class ManualEntryState(
    val foodName: String = "",
    val calories: String = "",
    val portionGrams: String = "100",
    val proteinG: String = "",
    val fatG: String = "",
    val carbsG: String = "",
)

data class AddFoodUiState(
    val mealSlot: MealSlot = MealSlot.MORNING,
    val selectedTab: AddFoodTab = AddFoodTab.SEARCH,
    val searchQuery: String = "",
    val frequentSuggestions: List<FrequentFoodEntity> = emptyList(),
    val isSearching: Boolean = false,
    val searchResults: List<FoodSearchResult> = emptyList(),
    val hasSearchedOpenFoodFacts: Boolean = false,
    val hasSearchedNutritionix: Boolean = false,
    val showFallbackOptions: Boolean = false,
    val selectedFood: SelectedFood? = null,
    val portionGrams: Double = 100.0,
    val showManualEntryForm: Boolean = false,
    val manualEntry: ManualEntryState = ManualEntryState(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val scanErrorMessage: String? = null,
) {
    val caloriesForPortion: Double?
        get() = selectedFood?.let { it.caloriesPer100g * portionGrams / 100.0 }
}

@OptIn(FlowPreview::class)
@HiltViewModel
class AddFoodViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val foodDiaryRepository: FoodDiaryRepository,
    private val foodSearchRepository: FoodSearchRepository,
    private val frequentFoodsRepository: FrequentFoodsRepository,
    private val foodLookupRepository: FoodLookupRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddFoodUiState(
            mealSlot = savedStateHandle.get<String>(Screen.AddFood.ARG_MEAL_SLOT)
                ?.let { runCatching { MealSlot.valueOf(it) }.getOrNull() }
                ?: MealSlot.MORNING,
        ),
    )
    val uiState: StateFlow<AddFoodUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            combine(
                searchQueryFlow.debounce(200),
                frequentFoodsRepository.observeTopFrequentFoods(50),
            ) { query, allFrequent ->
                if (query.isBlank()) {
                    allFrequent.take(5)
                } else {
                    allFrequent.filter { it.foodName.contains(query, ignoreCase = true) }.take(5)
                }
            }.collect { suggestions -> _uiState.update { it.copy(frequentSuggestions = suggestions) } }
        }
    }

    fun selectTab(tab: AddFoodTab) = _uiState.update { it.copy(selectedTab = tab) }

    fun selectMealSlot(mealSlot: MealSlot) = _uiState.update { it.copy(mealSlot = mealSlot) }

    fun updateSearchQuery(query: String) {
        searchQueryFlow.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun search() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSearching = true,
                    selectedFood = null,
                    showManualEntryForm = false,
                    hasSearchedNutritionix = false,
                )
            }
            val results = foodSearchRepository.searchOpenFoodFacts(query)
            _uiState.update {
                it.copy(
                    isSearching = false,
                    hasSearchedOpenFoodFacts = true,
                    searchResults = results,
                    showFallbackOptions = results.isEmpty(),
                )
            }
        }
    }

    fun searchNutritionix() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            val results = foodSearchRepository.searchNutritionix(query)
            _uiState.update {
                it.copy(
                    isSearching = false,
                    hasSearchedNutritionix = true,
                    searchResults = results,
                    showFallbackOptions = results.isEmpty(),
                )
            }
        }
    }

    fun selectSearchResult(result: FoodSearchResult) {
        _uiState.update {
            it.copy(
                selectedFood = result.toSelectedFood(),
                portionGrams = 100.0,
                showManualEntryForm = false,
            )
        }
    }

    fun selectFrequentSuggestion(food: FrequentFoodEntity) {
        _uiState.update {
            it.copy(
                selectedFood = food.toSelectedFood(),
                portionGrams = food.averageQuantity,
                showManualEntryForm = false,
            )
        }
    }

    fun adjustPortion(deltaGrams: Double) {
        _uiState.update { it.copy(portionGrams = (it.portionGrams + deltaGrams).coerceAtLeast(1.0)) }
    }

    fun setPortionGrams(grams: Double) {
        _uiState.update { it.copy(portionGrams = grams.coerceAtLeast(1.0)) }
    }

    fun clearSelectedFood() = _uiState.update { it.copy(selectedFood = null) }

    fun showManualEntry() {
        _uiState.update { it.copy(showManualEntryForm = true, selectedFood = null) }
    }

    fun hideManualEntry() = _uiState.update { it.copy(showManualEntryForm = false) }

    fun updateManualFoodName(value: String) =
        _uiState.update { it.copy(manualEntry = it.manualEntry.copy(foodName = value)) }

    fun updateManualCalories(value: String) =
        _uiState.update { it.copy(manualEntry = it.manualEntry.copy(calories = value)) }

    fun updateManualPortion(value: String) =
        _uiState.update { it.copy(manualEntry = it.manualEntry.copy(portionGrams = value)) }

    fun updateManualProtein(value: String) =
        _uiState.update { it.copy(manualEntry = it.manualEntry.copy(proteinG = value)) }

    fun updateManualFat(value: String) =
        _uiState.update { it.copy(manualEntry = it.manualEntry.copy(fatG = value)) }

    fun updateManualCarbs(value: String) =
        _uiState.update { it.copy(manualEntry = it.manualEntry.copy(carbsG = value)) }

    fun onBarcodeScanned(barcode: String) {
        viewModelScope.launch {
            when (val result = foodLookupRepository.lookupBarcode(barcode)) {
                is FoodLookupResult.Found -> _uiState.update {
                    it.copy(
                        selectedFood = result.food.toSelectedFood(),
                        portionGrams = 100.0,
                        scanErrorMessage = null,
                    )
                }
                is FoodLookupResult.NotFound -> _uiState.update {
                    it.copy(scanErrorMessage = "No product found for that barcode.")
                }
                is FoodLookupResult.Error -> _uiState.update {
                    it.copy(scanErrorMessage = "Lookup failed - check your connection and try again.")
                }
            }
        }
    }

    fun logSelectedFood() {
        val state = _uiState.value
        val food = state.selectedFood ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            foodDiaryRepository.logFood(food.toFoodEntry(state.portionGrams, state.mealSlot))
            frequentFoodsRepository.recordLog(food.toFrequentFoodEntity(state.portionGrams))
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    fun logManualEntry() {
        val entry = _uiState.value.manualEntry
        val mealSlot = _uiState.value.mealSlot
        val foodName = entry.foodName.trim().ifBlank { return }
        val calories = entry.calories.toDoubleOrNull() ?: return
        val portionGrams = entry.portionGrams.toDoubleOrNull()?.takeIf { it > 0 } ?: return
        val proteinG = entry.proteinG.toDoubleOrNull() ?: 0.0
        val fatG = entry.fatG.toDoubleOrNull() ?: 0.0
        val carbsG = entry.carbsG.toDoubleOrNull() ?: 0.0
        val per100gFactor = 100.0 / portionGrams

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            foodDiaryRepository.logFood(
                FoodEntryEntity(
                    date = LocalDate.now(),
                    mealSlot = mealSlot,
                    foodName = foodName,
                    barcode = null,
                    quantity = portionGrams,
                    quantityUnit = QuantityUnit.GRAMS,
                    calories = calories,
                    proteinG = proteinG,
                    fatG = fatG,
                    carbsG = carbsG,
                    source = DataSource.MANUAL,
                    timestamp = Instant.now(),
                ),
            )
            frequentFoodsRepository.recordLog(
                FrequentFoodEntity(
                    foodName = foodName,
                    barcode = null,
                    averageQuantity = portionGrams,
                    quantityUnit = QuantityUnit.GRAMS,
                    caloriesPer100g = calories * per100gFactor,
                    proteinPer100g = proteinG * per100gFactor,
                    fatPer100g = fatG * per100gFactor,
                    carbsPer100g = carbsG * per100gFactor,
                    source = DataSource.MANUAL,
                ),
            )
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }
}

private fun FoodSearchResult.toSelectedFood() = SelectedFood(
    name = name,
    barcode = barcode,
    caloriesPer100g = caloriesPer100g,
    proteinPer100g = proteinPer100g,
    fatPer100g = fatPer100g,
    carbsPer100g = carbsPer100g,
    source = source,
)

private fun FrequentFoodEntity.toSelectedFood() = SelectedFood(
    name = foodName,
    barcode = barcode,
    caloriesPer100g = caloriesPer100g,
    proteinPer100g = proteinPer100g,
    fatPer100g = fatPer100g,
    carbsPer100g = carbsPer100g,
    source = source,
)

private fun ScannedFood.toSelectedFood() = SelectedFood(
    name = name,
    barcode = barcode,
    caloriesPer100g = caloriesPer100g ?: 0.0,
    proteinPer100g = proteinPer100g ?: 0.0,
    fatPer100g = fatPer100g ?: 0.0,
    carbsPer100g = carbsPer100g ?: 0.0,
    source = DataSource.OPEN_FOOD_FACTS,
)

private fun SelectedFood.toFoodEntry(portionGrams: Double, mealSlot: MealSlot) = FoodEntryEntity(
    date = LocalDate.now(),
    mealSlot = mealSlot,
    foodName = name,
    barcode = barcode,
    quantity = portionGrams,
    quantityUnit = QuantityUnit.GRAMS,
    calories = caloriesPer100g * portionGrams / 100.0,
    proteinG = proteinPer100g * portionGrams / 100.0,
    fatG = fatPer100g * portionGrams / 100.0,
    carbsG = carbsPer100g * portionGrams / 100.0,
    source = source,
    timestamp = Instant.now(),
)

private fun SelectedFood.toFrequentFoodEntity(portionGrams: Double) = FrequentFoodEntity(
    foodName = name,
    barcode = barcode,
    averageQuantity = portionGrams,
    quantityUnit = QuantityUnit.GRAMS,
    caloriesPer100g = caloriesPer100g,
    proteinPer100g = proteinPer100g,
    fatPer100g = fatPer100g,
    carbsPer100g = carbsPer100g,
    source = source,
)
