package com.nutritrack.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutritrack.app.BuildConfig

// Hidden developer tool: fires each notification worker immediately, bypassing its real schedule,
// so its content/behavior can be checked without waiting for Sunday 9am etc. Gated behind
// BuildConfig.DEBUG so it never appears in a release build - to remove entirely, delete this file,
// DeveloperTestViewModel.kt, and the DeveloperTestSection() call in SettingsScreen.kt.
@Composable
fun DeveloperTestSection(modifier: Modifier = Modifier, viewModel: DeveloperTestViewModel = hiltViewModel()) {
    if (!BuildConfig.DEBUG) return

    val supplements by viewModel.supplements.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        Text("Developer Test Tools", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
        Text(
            "Debug build only - fires each notification immediately for testing. Not shown in release builds.",
            style = MaterialTheme.typography.bodySmall,
        )

        OutlinedButton(onClick = viewModel::triggerMealGapReminder, modifier = Modifier.fillMaxWidth()) {
            Text("Trigger Meal Gap Reminder")
        }
        OutlinedButton(onClick = viewModel::triggerBpReminder, modifier = Modifier.fillMaxWidth()) {
            Text("Trigger BP Reminder")
        }
        OutlinedButton(onClick = viewModel::triggerWeighInReminder, modifier = Modifier.fillMaxWidth()) {
            Text("Trigger Weigh-In Prompt")
        }

        if (supplements.isEmpty()) {
            Text("No supplements added yet - add one to test its reminder.", style = MaterialTheme.typography.bodySmall)
        } else {
            Text("Supplement Reminders", style = MaterialTheme.typography.labelLarge)
            supplements.forEach { supplement ->
                OutlinedButton(
                    onClick = { viewModel.triggerSupplementReminder(supplement.id) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Trigger: ${supplement.name}")
                }
            }
        }
    }
}
