package com.nutritrack.app.ui.screens.activitylog

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutritrack.app.data.local.entity.ActivityEntryEntity
import com.nutritrack.app.data.local.entity.ActivityType
import com.nutritrack.app.data.local.entity.Intensity
import com.nutritrack.app.domain.activity.ActivityMetDefaults
import com.nutritrack.app.ui.theme.PaceGreen
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogScreen(
    modifier: Modifier = Modifier,
    viewModel: ActivityLogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Activity") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ActivityTypeGrid(selected = uiState.selectedActivityType, onSelect = viewModel::selectActivityType)

            DurationInput(minutes = uiState.durationMinutes, onChange = viewModel::updateDurationMinutes)

            WeightDisplay(weightKg = uiState.weightKg)

            IntensitySelector(selected = uiState.intensity, onSelect = viewModel::selectIntensity)

            if (uiState.selectedActivityType != null) {
                ResultCard(uiState = uiState)
                uiState.hydrationTip?.let { HydrationTipCard(tip = it) }
            }

            Button(
                onClick = viewModel::logActivity,
                enabled = uiState.canSave && !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save")
            }

            RecentActivitiesList(entries = uiState.recentActivities, onRelog = viewModel::reLogActivity)
        }
    }
}

@Composable
private fun ActivityTypeGrid(
    selected: ActivityType?,
    onSelect: (ActivityType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActivityCard(ActivityType.TENNIS, selected == ActivityType.TENNIS, onSelect, Modifier.weight(1f))
            ActivityCard(ActivityType.PADEL, selected == ActivityType.PADEL, onSelect, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActivityCard(ActivityType.WALKING, selected == ActivityType.WALKING, onSelect, Modifier.weight(1f))
            ActivityCard(
                ActivityType.INDOOR_CYCLING,
                selected == ActivityType.INDOOR_CYCLING,
                onSelect,
                Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ActivityCard(
    activityType: ActivityType,
    isSelected: Boolean,
    onSelect: (ActivityType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = { onSelect(activityType) },
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .aspectRatio(1f)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) PaceGreen else MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.medium,
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(activityType.icon(), contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(activityType.displayLabel(), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
            Text(
                "${ActivityMetDefaults.baseMet(activityType)} MET",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun DurationInput(minutes: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    var text by remember(minutes) { mutableStateOf(if (minutes == 0) "" else minutes.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onChange(it.toIntOrNull() ?: 0)
        },
        label = { Text("Duration (minutes)") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun WeightDisplay(weightKg: Double?, modifier: Modifier = Modifier) {
    Text(
        weightKg?.let { "Using weight: ${it.roundToInt()} kg (from your profile)" }
            ?: "Set your weight in your profile to calculate calories burned.",
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier,
    )
}

@Composable
private fun IntensitySelector(selected: Intensity, onSelect: (Intensity) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Intensity", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Intensity.entries.forEach { intensity ->
                if (intensity == selected) {
                    Button(onClick = { onSelect(intensity) }, modifier = Modifier.weight(1f)) {
                        Text(intensity.displayLabel())
                    }
                } else {
                    OutlinedButton(onClick = { onSelect(intensity) }, modifier = Modifier.weight(1f)) {
                        Text(intensity.displayLabel())
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultCard(uiState: ActivityLogUiState, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Calories Burned", style = MaterialTheme.typography.labelLarge)
            Text("${uiState.pendingCaloriesBurned?.roundToInt() ?: 0} kcal", style = MaterialTheme.typography.headlineMedium)
            uiState.adjustedMet?.let { met ->
                Text(
                    "${"%.1f".format(met)} MET x ${uiState.weightKg?.roundToInt() ?: 0}kg x " +
                        "${uiState.durationMinutes}min / 60",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            uiState.adjustedDailyCalorieTarget?.let {
                Text("Adjusted daily target: ${it.roundToInt()} kcal", style = MaterialTheme.typography.bodyMedium)
            }
            uiState.remainingCaloriesAfterActivity?.let {
                Text("Remaining after activity: ${it.roundToInt()} kcal", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun HydrationTipCard(tip: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.WaterDrop, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(tip, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RecentActivitiesList(
    entries: List<ActivityEntryEntity>,
    onRelog: (ActivityEntryEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Recent Activities", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        entries.forEach { entry -> RecentActivityRow(entry = entry, onClick = { onRelog(entry) }) }
    }
}

@Composable
private fun RecentActivityRow(entry: ActivityEntryEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(entry.activityType.displayLabel(), style = MaterialTheme.typography.bodyMedium)
            Text(
                "${entry.date} - ${entry.durationMinutes}min - ${entry.caloriesBurned.roundToInt()} kcal",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        TextButton(onClick = onClick) { Text("Re-log") }
    }
}

private fun ActivityType.displayLabel(): String = when (this) {
    ActivityType.TENNIS -> "Tennis"
    ActivityType.PADEL -> "Padel"
    ActivityType.WALKING -> "Walking"
    ActivityType.INDOOR_CYCLING -> "Indoor Cycling"
}

private fun ActivityType.icon(): ImageVector = when (this) {
    ActivityType.TENNIS -> Icons.Filled.SportsTennis
    ActivityType.PADEL -> Icons.Filled.SportsVolleyball
    ActivityType.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
    ActivityType.INDOOR_CYCLING -> Icons.AutoMirrored.Filled.DirectionsBike
}

private fun Intensity.displayLabel(): String = when (this) {
    Intensity.LIGHT -> "Light"
    Intensity.MODERATE -> "Moderate"
    Intensity.INTENSE -> "Intense"
}
