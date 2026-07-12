package com.nutritrack.app.ui.screens.profilesetup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.nutritrack.app.data.local.entity.ActivityLevel
import com.nutritrack.app.data.local.entity.BiologicalSex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileSetupViewModel = hiltViewModel(),
) {
    var heightText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var targetWeightText by remember { mutableStateOf("") }
    var deadlineText by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Set Up Your Profile") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(value = heightText, onValueChange = { heightText = it }, label = { Text("Height (cm)") })
            OutlinedTextField(value = weightText, onValueChange = { weightText = it }, label = { Text("Weight (kg)") })
            OutlinedTextField(value = ageText, onValueChange = { ageText = it }, label = { Text("Age") })
            OutlinedTextField(
                value = targetWeightText,
                onValueChange = { targetWeightText = it },
                label = { Text("Target weight (kg)") },
            )
            OutlinedTextField(
                value = deadlineText,
                onValueChange = { deadlineText = it },
                label = { Text("Goal deadline (weeks)") },
            )
            Button(onClick = {
                val height = heightText.toDoubleOrNull() ?: return@Button
                val weight = weightText.toDoubleOrNull() ?: return@Button
                val age = ageText.toIntOrNull() ?: return@Button
                val targetWeight = targetWeightText.toDoubleOrNull() ?: return@Button
                val deadlineWeeks = deadlineText.toIntOrNull() ?: return@Button

                viewModel.updateHeightCm(height)
                viewModel.updateWeightKg(weight)
                viewModel.updateAge(age)
                // Sex and activity level pickers aren't built yet; defaulted until this screen gets real inputs.
                viewModel.updateBiologicalSex(BiologicalSex.OTHER)
                viewModel.updateActivityLevel(ActivityLevel.MODERATELY_ACTIVE)
                viewModel.updateTargetWeightKg(targetWeight)
                viewModel.updateGoalDeadlineWeeks(deadlineWeeks)
                viewModel.saveProfile()
                onSetupComplete()
            }) {
                Text("Get Started")
            }
        }
    }
}
