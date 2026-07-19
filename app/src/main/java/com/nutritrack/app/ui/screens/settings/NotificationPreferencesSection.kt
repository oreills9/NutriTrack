package com.nutritrack.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NotificationPreferencesSection(uiState: SettingsUiState, viewModel: SettingsViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ReminderToggleRow(
            title = "Meal Gap Reminder",
            subtitle = "Notify me if I haven't logged a meal in a while",
            checked = uiState.mealGapReminderEnabled,
            onCheckedChange = viewModel::setMealGapReminderEnabled,
        ) {
            HoursStepper(hours = uiState.mealGapReminderHours, onHoursChange = viewModel::setMealGapReminderHours)
        }

        ReminderToggleRow(
            title = "Sunday BP Reminder",
            subtitle = "Weekly blood pressure check-in",
            checked = uiState.sundayBpReminderEnabled,
            onCheckedChange = viewModel::setSundayBpReminderEnabled,
        ) {
            TimePickerRow(time = uiState.sundayBpReminderTime, onTimeChange = viewModel::setSundayBpReminderTime)
        }

        ReminderToggleRow(
            title = "Sunday Weigh-In Prompt",
            subtitle = "Weekly reminder to log your weight",
            checked = uiState.sundayWeighInReminderEnabled,
            onCheckedChange = viewModel::setSundayWeighInReminderEnabled,
        ) {
            TimePickerRow(time = uiState.sundayWeighInReminderTime, onTimeChange = viewModel::setSundayWeighInReminderTime)
        }

        ReminderToggleRow(
            title = "Supplement Reminders",
            subtitle = "Daily nudge for anything not yet taken",
            checked = uiState.supplementReminderEnabled,
            onCheckedChange = viewModel::setSupplementReminderEnabled,
        ) {
            TimePickerRow(time = uiState.supplementReminderTime, onTimeChange = viewModel::setSupplementReminderTime)
        }
    }
}

@Composable
private fun ReminderToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    detail: @Composable () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
        if (checked) {
            Spacer(modifier = Modifier.height(8.dp))
            detail()
        }
    }
}

@Composable
private fun HoursStepper(hours: Int, onHoursChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("Remind after", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedButton(onClick = { onHoursChange((hours - 1).coerceIn(MIN_MEAL_GAP_HOURS, MAX_MEAL_GAP_HOURS)) }) { Text("-") }
        Text("$hours h", modifier = Modifier.padding(horizontal = 12.dp))
        OutlinedButton(onClick = { onHoursChange((hours + 1).coerceIn(MIN_MEAL_GAP_HOURS, MAX_MEAL_GAP_HOURS)) }) { Text("+") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerRow(time: LocalTime, onTimeChange: (LocalTime) -> Unit, modifier: Modifier = Modifier) {
    var showPicker by remember { mutableStateOf(false) }
    val timeFormat = remember { DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()) }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("Time: ${time.format(timeFormat)}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(onClick = { showPicker = true }) { Text("Change") }
    }

    if (showPicker) {
        val pickerState = rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = false)
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChange(LocalTime.of(pickerState.hour, pickerState.minute))
                    showPicker = false
                }) { Text("Set") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } },
            text = { TimePicker(state = pickerState) },
        )
    }
}

private const val MIN_MEAL_GAP_HOURS = 1
private const val MAX_MEAL_GAP_HOURS = 12
