package com.nutritrack.app.ui.screens.fooddiary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    onAddFood: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSupplements: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FoodDiaryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Diary") },
                actions = {
                    IconButton(onClick = onOpenSupplements) {
                        Icon(Icons.Filled.Medication, contentDescription = "Supplements")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddFood) {
                Icon(Icons.Filled.Add, contentDescription = "Add food")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Calories consumed: ${uiState.totalCaloriesConsumed.roundToInt()}")
            uiState.dailyCalorieTarget?.let { target -> Text("Daily target: $target") }
            uiState.remainingCalories?.let { remaining -> Text("Remaining: ${remaining.roundToInt()}") }
            uiState.paceIndicator?.let { pace -> Text("Pace: $pace") }
            val entryCount = uiState.entriesByMealSlot.values.sumOf { it.size }
            Text("Entries logged today: $entryCount")
        }
    }
}
