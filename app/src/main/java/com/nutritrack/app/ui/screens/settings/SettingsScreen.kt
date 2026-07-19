package com.nutritrack.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutritrack.app.data.local.entity.ActivityLevel
import com.nutritrack.app.data.local.entity.BiologicalSex
import com.nutritrack.app.data.local.entity.UnitSystem
import com.nutritrack.app.domain.units.UnitConverter

private val ACTIVITY_LEVEL_OPTIONS = listOf(
    ActivityLevel.SEDENTARY to "Sedentary",
    ActivityLevel.LIGHTLY_ACTIVE to "Lightly Active",
    ActivityLevel.MODERATELY_ACTIVE to "Moderately Active",
    ActivityLevel.VERY_ACTIVE to "Very Active",
    ActivityLevel.EXTRA_ACTIVE to "Extra Active",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onDataReset: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.resetComplete) {
        if (uiState.resetComplete) onDataReset()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            UnitsSection(selected = uiState.preferredUnits, onSelected = viewModel::updatePreferredUnits)

            SectionHeader("Personal Details")
            PersonalDetailsSection(uiState = uiState, viewModel = viewModel)

            SectionHeader("Goal Settings")
            GoalSettingsSection(uiState = uiState, viewModel = viewModel)

            uiState.dailyCalorieTarget?.let {
                Text("Current daily calorie target: $it kcal", style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = viewModel::saveProfile,
                enabled = uiState.isPersonalDetailsValid && uiState.isGoalSettingsValid && !uiState.isSavingProfile,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save Personal & Goal Details")
            }
            uiState.profileSavedMessage?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }

            SectionHeader("Notification Preferences")
            NotificationPreferencesSection(uiState = uiState, viewModel = viewModel)

            SectionHeader("Nutritionix API")
            NutritionixSection(uiState = uiState, viewModel = viewModel)

            SectionHeader("Data Management")
            DataManagementSection(uiState = uiState, viewModel = viewModel)

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "NutriTrack ${uiState.appVersion}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun UnitsSection(selected: UnitSystem, onSelected: (UnitSystem) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Units", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectableOptionButton(
                label = "Metric (kg, cm)",
                isSelected = selected == UnitSystem.METRIC,
                onClick = { onSelected(UnitSystem.METRIC) },
                modifier = Modifier.weight(1f),
            )
            SelectableOptionButton(
                label = "Imperial (lbs, ft/in)",
                isSelected = selected == UnitSystem.IMPERIAL,
                onClick = { onSelected(UnitSystem.IMPERIAL) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SelectableOptionButton(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (isSelected) {
        Button(onClick = onClick, modifier = modifier) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(label) }
    }
}

@Composable
private fun PersonalDetailsSection(uiState: SettingsUiState, viewModel: SettingsViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (uiState.preferredUnits == UnitSystem.METRIC) {
            HeightMetricField(uiState.heightCm, viewModel::updateHeightCm)
            WeightMetricField("Weight (kg)", uiState.weightKg, viewModel::updateWeightKg)
        } else {
            HeightImperialField(uiState.heightCm, viewModel::updateHeightCm)
            WeightImperialField("Weight (lbs)", uiState.weightKg, viewModel::updateWeightKg)
        }
        AgeField(uiState.age, viewModel::updateAge)
        BiologicalSexSelector(uiState.biologicalSex, viewModel::updateBiologicalSex)
        ActivityLevelSelector(uiState.activityLevel, viewModel::updateActivityLevel)
    }
}

@Composable
private fun GoalSettingsSection(uiState: SettingsUiState, viewModel: SettingsViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (uiState.preferredUnits == UnitSystem.METRIC) {
            WeightMetricField("Target Weight (kg)", uiState.targetWeightKg, viewModel::updateTargetWeightKg)
        } else {
            WeightImperialField("Target Weight (lbs)", uiState.targetWeightKg, viewModel::updateTargetWeightKg)
        }
        IntField("Deadline (weeks)", uiState.goalDeadlineWeeks, viewModel::updateGoalDeadlineWeeks)
    }
}

@Composable
private fun NutritionixSection(uiState: SettingsUiState, viewModel: SettingsViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Paste your free Nutritionix API credentials to enable natural-language food search as a fallback to Open Food Facts.",
            style = MaterialTheme.typography.bodySmall,
        )
        SecretField("Nutritionix App ID", uiState.nutritionixAppId, viewModel::updateNutritionixAppId)
        SecretField("Nutritionix App Key", uiState.nutritionixAppKey, viewModel::updateNutritionixAppKey)
        Button(
            onClick = viewModel::saveNutritionixKeys,
            enabled = !uiState.isSavingNutritionixKeys,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save Nutritionix Credentials")
        }
        uiState.nutritionixSavedMessage?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SecretField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) "Hide $label" else "Show $label",
                )
            }
        },
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun HeightMetricField(heightCm: Double?, onChange: (Double?) -> Unit, modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf(heightCm?.let(::formatNumber) ?: "") }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onChange(it.toDoubleOrNull())
        },
        label = { Text("Height (cm)") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun HeightImperialField(heightCm: Double?, onChange: (Double?) -> Unit, modifier: Modifier = Modifier) {
    val initial = heightCm?.let(UnitConverter::cmToFeetInches)
    var feetText by remember { mutableStateOf(initial?.first?.toString() ?: "") }
    var inchesText by remember { mutableStateOf(initial?.second?.let(::formatNumber) ?: "") }

    fun push(feet: String, inches: String) {
        val feetValue = feet.toIntOrNull()
        val inchesValue = inches.toDoubleOrNull()
        onChange(if (feetValue != null && inchesValue != null) UnitConverter.feetInchesToCm(feetValue, inchesValue) else null)
    }

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = feetText,
            onValueChange = { feetText = it; push(it, inchesText) },
            label = { Text("Feet") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = inchesText,
            onValueChange = { inchesText = it; push(feetText, it) },
            label = { Text("Inches") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun WeightMetricField(label: String, weightKg: Double?, onChange: (Double?) -> Unit, modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf(weightKg?.let(::formatNumber) ?: "") }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onChange(it.toDoubleOrNull())
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun WeightImperialField(label: String, weightKg: Double?, onChange: (Double?) -> Unit, modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf(weightKg?.let { formatNumber(UnitConverter.kgToLbs(it)) } ?: "") }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onChange(it.toDoubleOrNull()?.let(UnitConverter::lbsToKg))
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun AgeField(age: Int?, onChange: (Int?) -> Unit, modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf(age?.toString() ?: "") }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onChange(it.toIntOrNull())
        },
        label = { Text("Age") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun IntField(label: String, value: Int?, onChange: (Int?) -> Unit, modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf(value?.toString() ?: "") }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onChange(it.toIntOrNull())
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun BiologicalSexSelector(selected: BiologicalSex?, onSelected: (BiologicalSex) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Biological Sex", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectableOptionButton("Male", selected == BiologicalSex.MALE, { onSelected(BiologicalSex.MALE) }, Modifier.weight(1f))
            SelectableOptionButton("Female", selected == BiologicalSex.FEMALE, { onSelected(BiologicalSex.FEMALE) }, Modifier.weight(1f))
            SelectableOptionButton("Other", selected == BiologicalSex.OTHER, { onSelected(BiologicalSex.OTHER) }, Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityLevelSelector(selected: ActivityLevel?, onSelected: (ActivityLevel) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val label = ACTIVITY_LEVEL_OPTIONS.firstOrNull { it.first == selected }?.second ?: ""

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Activity Level") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ACTIVITY_LEVEL_OPTIONS.forEach { (level, text) ->
                DropdownMenuItem(text = { Text(text) }, onClick = { onSelected(level); expanded = false })
            }
        }
    }
}

private fun formatNumber(value: Double): String =
    if (value == value.toInt().toDouble()) value.toInt().toString() else value.toString()
