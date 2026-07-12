package com.nutritrack.app.ui.screens.addfood

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutritrack.app.data.local.entity.MealSlot
import kotlin.math.roundToInt

private val PORTION_PRESETS_GRAMS = listOf(50.0, 100.0, 150.0, 200.0)
private const val PORTION_STEP_GRAMS = 10.0

@Composable
fun SelectedFoodDetail(
    food: SelectedFood,
    portionGrams: Double,
    caloriesForPortion: Double?,
    mealSlot: MealSlot,
    isSaving: Boolean,
    onPortionDelta: (Double) -> Unit,
    onPortionPreset: (Double) -> Unit,
    onMealSlotSelected: (MealSlot) -> Unit,
    onLog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                Text(food.name, style = MaterialTheme.typography.titleMedium)
                food.barcode?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }

            Text(
                "Per 100g - ${food.caloriesPer100g.roundToInt()} kcal, " +
                    "P ${food.proteinPer100g.roundToInt()}g, " +
                    "F ${food.fatPer100g.roundToInt()}g, " +
                    "C ${food.carbsPer100g.roundToInt()}g",
                style = MaterialTheme.typography.bodySmall,
            )

            PortionAdjuster(
                portionGrams = portionGrams,
                onPortionDelta = onPortionDelta,
                onPortionPreset = onPortionPreset,
            )

            Text(
                "${caloriesForPortion?.roundToInt() ?: 0} kcal for ${portionGrams.roundToInt()}g",
                style = MaterialTheme.typography.titleMedium,
            )

            MealSlotSelector(selected = mealSlot, onSelected = onMealSlotSelected)

            Button(onClick = onLog, enabled = !isSaving, modifier = Modifier.fillMaxWidth()) {
                Text("Log")
            }
        }
    }
}

@Composable
private fun PortionAdjuster(
    portionGrams: Double,
    onPortionDelta: (Double) -> Unit,
    onPortionPreset: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Portion", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { onPortionDelta(-PORTION_STEP_GRAMS) }) { Text("-") }
            Text(
                "${portionGrams.roundToInt()}g",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            OutlinedButton(onClick = { onPortionDelta(PORTION_STEP_GRAMS) }) { Text("+") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PORTION_PRESETS_GRAMS.forEach { preset ->
                FilledTonalButton(onClick = { onPortionPreset(preset) }) {
                    Text("${preset.roundToInt()}g")
                }
            }
        }
    }
}

@Composable
internal fun MealSlotSelector(
    selected: MealSlot,
    onSelected: (MealSlot) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Meal Slot", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MealSlot.entries.forEach { slot ->
                if (slot == selected) {
                    Button(onClick = { onSelected(slot) }) { Text(slot.displayLabel()) }
                } else {
                    OutlinedButton(onClick = { onSelected(slot) }) { Text(slot.displayLabel()) }
                }
            }
        }
    }
}

internal fun MealSlot.displayLabel(): String = when (this) {
    MealSlot.MORNING -> "Morning"
    MealSlot.AFTERNOON -> "Afternoon"
    MealSlot.EVENING -> "Evening"
    MealSlot.ACTIVITY_SNACK -> "Activity Snack"
}
