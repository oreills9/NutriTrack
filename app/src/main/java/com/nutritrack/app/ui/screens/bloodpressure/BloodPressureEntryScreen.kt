package com.nutritrack.app.ui.screens.bloodpressure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nutritrack.app.data.local.entity.TimeOfDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodPressureEntryScreen(
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BloodPressureViewModel = hiltViewModel(),
) {
    var systolicText by remember { mutableStateOf("") }
    var diastolicText by remember { mutableStateOf("") }
    var heartRateText by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Log Blood Pressure") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(value = systolicText, onValueChange = { systolicText = it }, label = { Text("Systolic") })
            OutlinedTextField(value = diastolicText, onValueChange = { diastolicText = it }, label = { Text("Diastolic") })
            OutlinedTextField(
                value = heartRateText,
                onValueChange = { heartRateText = it },
                label = { Text("Heart rate (BPM)") },
            )
            Button(onClick = {
                val systolic = systolicText.toIntOrNull() ?: return@Button
                val diastolic = diastolicText.toIntOrNull() ?: return@Button
                val heartRate = heartRateText.toIntOrNull() ?: return@Button
                // Time-of-day picker isn't built yet; defaults to Morning.
                viewModel.logReading(systolic, diastolic, heartRate, TimeOfDay.MORNING)
                onSaved()
            }) {
                Text("Save")
            }
        }
    }
}
