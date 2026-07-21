package com.nutritrack.app.ui.screens.addfood

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nutritrack.app.data.local.entity.FrequentFoodEntity
import com.nutritrack.app.data.repository.FoodSearchResult
import kotlin.math.roundToInt

@Composable
fun SearchTabContent(uiState: AddFoodUiState, viewModel: AddFoodViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        SearchField(
            query = uiState.searchQuery,
            onQueryChange = viewModel::updateSearchQuery,
            onSearch = viewModel::search,
        )

        if (uiState.selectedFood == null && !uiState.showManualEntryForm) {
            FrequentSuggestionsRow(
                suggestions = uiState.frequentSuggestions,
                onSelect = viewModel::selectFrequentSuggestion,
            )
        }

        if (uiState.isSearching) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }

        when {
            uiState.selectedFood != null -> {
                Spacer(modifier = Modifier.height(12.dp))
                SelectedFoodDetail(
                    food = uiState.selectedFood,
                    portionGrams = uiState.portionGrams,
                    caloriesForPortion = uiState.caloriesForPortion,
                    mealSlot = uiState.mealSlot,
                    isSaving = uiState.isSaving,
                    onPortionDelta = viewModel::adjustPortion,
                    onPortionPreset = viewModel::setPortionGrams,
                    onMealSlotSelected = viewModel::selectMealSlot,
                    onLog = viewModel::logSelectedFood,
                )
            }
            uiState.showManualEntryForm -> {
                Spacer(modifier = Modifier.height(12.dp))
                ManualEntryForm(uiState = uiState, viewModel = viewModel)
            }
            uiState.showFallbackOptions -> {
                Spacer(modifier = Modifier.height(12.dp))
                if (uiState.hasSearchedNutritionix) {
                    Text("No results found.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = viewModel::showManualEntry, modifier = Modifier.fillMaxWidth()) {
                        Text("Enter Manually")
                    }
                } else {
                    FallbackOptionsRow(
                        onTryNutritionix = viewModel::searchNutritionix,
                        onEnterManually = viewModel::showManualEntry,
                    )
                }
            }
            uiState.searchResults.isNotEmpty() -> {
                Spacer(modifier = Modifier.height(12.dp))
                SearchResultsList(results = uiState.searchResults, onSelect = viewModel::selectSearchResult)
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Describe a food") },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onSearch, enabled = query.isNotBlank()) { Text("Search") }
    }
}

@Composable
private fun FrequentSuggestionsRow(
    suggestions: List<FrequentFoodEntity>,
    onSelect: (FrequentFoodEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (suggestions.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth().padding(top = 12.dp)) {
        Text("Frequent Foods", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            suggestions.forEach { food ->
                SuggestionChip(onClick = { onSelect(food) }, label = { Text(food.foodName) })
            }
        }
    }
}

@Composable
private fun SearchResultsList(
    results: List<FoodSearchResult>,
    onSelect: (FoodSearchResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        results.forEach { result ->
            SearchResultCard(result = result, onClick = { onSelect(result) })
        }
    }
}

@Composable
private fun SearchResultCard(result: FoodSearchResult, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(result.name, style = MaterialTheme.typography.titleSmall)
            result.brand?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            Text("${result.caloriesPer100g.roundToInt()} kcal / 100g", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun FallbackOptionsRow(
    onTryNutritionix: () -> Unit,
    onEnterManually: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("No results found", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onTryNutritionix, modifier = Modifier.weight(1f)) { Text("Try Nutritionix") }
            OutlinedButton(onClick = onEnterManually, modifier = Modifier.weight(1f)) { Text("Enter Manually") }
        }
    }
}

@Composable
internal fun ManualEntryForm(uiState: AddFoodUiState, viewModel: AddFoodViewModel, modifier: Modifier = Modifier) {
    val entry = uiState.manualEntry
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Enter Manually", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = entry.foodName,
            onValueChange = viewModel::updateManualFoodName,
            label = { Text("Food name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = entry.calories,
            onValueChange = viewModel::updateManualCalories,
            label = { Text("Calories") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = entry.portionGrams,
            onValueChange = viewModel::updateManualPortion,
            label = { Text("Portion (g)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = entry.proteinG,
            onValueChange = viewModel::updateManualProtein,
            label = { Text("Protein (g, optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = entry.fatG,
            onValueChange = viewModel::updateManualFat,
            label = { Text("Fat (g, optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = entry.carbsG,
            onValueChange = viewModel::updateManualCarbs,
            label = { Text("Carbs (g, optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )

        MealSlotSelector(selected = uiState.mealSlot, onSelected = viewModel::selectMealSlot)

        Button(
            onClick = viewModel::logManualEntry,
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Log")
        }
    }
}
