package com.nutritrack.app.ui.screens.bloodpressure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun BloodPressureAnalysisScreen(
    onLogNewReading: () -> Unit,
    onViewWeightHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BloodPressureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Health") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onLogNewReading) {
                Icon(Icons.Filled.Add, contentDescription = "Log reading")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            uiState.eightWeekAverages?.let { averages ->
                Text(
                    "8-week avg: ${averages.averageSystolic.roundToInt()}/" +
                        "${averages.averageDiastolic.roundToInt()} mmHg, " +
                        "HR ${averages.averageHeartRateBpm.roundToInt()}",
                )
            }
            Text("Readings logged: ${uiState.readings.size}")
            TextButton(onClick = onViewWeightHistory) { Text("View weight history") }
        }
    }
}
