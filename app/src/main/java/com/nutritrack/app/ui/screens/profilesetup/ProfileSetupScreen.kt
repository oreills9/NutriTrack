package com.nutritrack.app.ui.screens.profilesetup

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutritrack.app.data.local.entity.ActivityLevel
import com.nutritrack.app.data.local.entity.BiologicalSex
import com.nutritrack.app.data.local.entity.UnitSystem
import com.nutritrack.app.domain.calorie.MacroSplit
import com.nutritrack.app.domain.units.UnitConverter

private val ACTIVITY_LEVEL_OPTIONS = listOf(
    ActivityLevel.SEDENTARY to "Sedentary",
    ActivityLevel.LIGHTLY_ACTIVE to "Lightly Active",
    ActivityLevel.MODERATELY_ACTIVE to "Moderately Active",
    ActivityLevel.VERY_ACTIVE to "Very Active",
)

private const val STEP_COUNT = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileSetupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentStep by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) onSetupComplete()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(title = { Text("Set Up Your Profile") })
                StepProgressIndicator(currentStep = currentStep)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            when (currentStep) {
                0 -> PersonalMeasurementsSection(uiState = uiState, viewModel = viewModel)
                1 -> WeightGoalSection(uiState = uiState, viewModel = viewModel)
                else -> ResultsSection(uiState = uiState)
            }

            Spacer(modifier = Modifier.height(24.dp))

            StepNavigationButtons(
                currentStep = currentStep,
                canAdvance = when (currentStep) {
                    0 -> uiState.isPersonalSectionValid
                    1 -> uiState.isGoalSectionValid
                    else -> true
                },
                isSaving = uiState.isSaving,
                onBack = { currentStep -= 1 },
                onNext = {
                    if (currentStep == 1) viewModel.calculatePreview()
                    currentStep += 1
                },
                onFinish = viewModel::completeSetup,
            )
        }
    }
}

@Composable
private fun StepProgressIndicator(currentStep: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Step ${currentStep + 1} of $STEP_COUNT", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (currentStep + 1) / STEP_COUNT.toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PersonalMeasurementsSection(
    uiState: ProfileSetupUiState,
    viewModel: ProfileSetupViewModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Personal Measurements", style = MaterialTheme.typography.titleLarge)

        UnitSystemToggle(selected = uiState.preferredUnits, onSelected = viewModel::updatePreferredUnits)

        if (uiState.preferredUnits == UnitSystem.METRIC) {
            HeightMetricInput(heightCm = uiState.heightCm, onHeightChanged = viewModel::updateHeightCm)
            WeightMetricInput(
                label = "Weight (kg)",
                weightKg = uiState.weightKg,
                onWeightChanged = viewModel::updateWeightKg,
            )
        } else {
            HeightImperialInput(heightCm = uiState.heightCm, onHeightChanged = viewModel::updateHeightCm)
            WeightImperialInput(
                label = "Weight (lbs)",
                weightKg = uiState.weightKg,
                onWeightChanged = viewModel::updateWeightKg,
            )
        }

        AgeInput(age = uiState.age, onAgeChanged = viewModel::updateAge)

        BiologicalSexToggle(selected = uiState.biologicalSex, onSelected = viewModel::updateBiologicalSex)

        ActivityLevelDropdown(selected = uiState.activityLevel, onSelected = viewModel::updateActivityLevel)
    }
}

@Composable
private fun WeightGoalSection(
    uiState: ProfileSetupUiState,
    viewModel: ProfileSetupViewModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Weight Goal", style = MaterialTheme.typography.titleLarge)

        if (uiState.preferredUnits == UnitSystem.METRIC) {
            WeightMetricInput(
                label = "Target Weight (kg)",
                weightKg = uiState.targetWeightKg,
                onWeightChanged = viewModel::updateTargetWeightKg,
            )
        } else {
            WeightImperialInput(
                label = "Target Weight (lbs)",
                weightKg = uiState.targetWeightKg,
                onWeightChanged = viewModel::updateTargetWeightKg,
            )
        }

        GoalDeadlineInput(weeks = uiState.goalDeadlineWeeks, onChanged = viewModel::updateGoalDeadlineWeeks)
    }
}

@Composable
private fun ResultsSection(uiState: ProfileSetupUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Your Plan", style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Daily Calorie Target", style = MaterialTheme.typography.labelLarge)
                Text("${uiState.dailyCalorieTarget ?: "-"} kcal", style = MaterialTheme.typography.headlineMedium)
                uiState.calculatedTdee?.let { tdee ->
                    Text("Maintenance (TDEE): $tdee kcal", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        uiState.macroSplit?.let { macros -> MacroSplitCard(macros) }
    }
}

@Composable
private fun MacroSplitCard(macros: MacroSplit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Suggested Daily Macros", style = MaterialTheme.typography.labelLarge)
            Text("Protein: ${macros.proteinGrams} g")
            Text("Fat: ${macros.fatGrams} g")
            Text("Carbs: ${macros.carbGrams} g")
        }
    }
}

@Composable
private fun StepNavigationButtons(
    currentStep: Int,
    canAdvance: Boolean,
    isSaving: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        if (currentStep > 0) {
            OutlinedButton(onClick = onBack, enabled = !isSaving) { Text("Back") }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        if (currentStep < STEP_COUNT - 1) {
            Button(onClick = onNext, enabled = canAdvance) { Text("Next") }
        } else {
            Button(onClick = onFinish, enabled = !isSaving) { Text("Get Started") }
        }
    }
}

@Composable
private fun UnitSystemToggle(selected: UnitSystem, onSelected: (UnitSystem) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Units", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectableOptionButton(
                label = "Metric",
                isSelected = selected == UnitSystem.METRIC,
                onClick = { onSelected(UnitSystem.METRIC) },
                modifier = Modifier.weight(1f),
            )
            SelectableOptionButton(
                label = "Imperial",
                isSelected = selected == UnitSystem.IMPERIAL,
                onClick = { onSelected(UnitSystem.IMPERIAL) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BiologicalSexToggle(
    selected: BiologicalSex?,
    onSelected: (BiologicalSex) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Biological Sex", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectableOptionButton(
                label = "Male",
                isSelected = selected == BiologicalSex.MALE,
                onClick = { onSelected(BiologicalSex.MALE) },
                modifier = Modifier.weight(1f),
            )
            SelectableOptionButton(
                label = "Female",
                isSelected = selected == BiologicalSex.FEMALE,
                onClick = { onSelected(BiologicalSex.FEMALE) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SelectableOptionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isSelected) {
        Button(onClick = onClick, modifier = modifier) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(label) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityLevelDropdown(
    selected: ActivityLevel?,
    onSelected: (ActivityLevel) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = ACTIVITY_LEVEL_OPTIONS.firstOrNull { it.first == selected }?.second ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Activity Level") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ACTIVITY_LEVEL_OPTIONS.forEach { (level, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelected(level)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun HeightMetricInput(heightCm: Double?, onHeightChanged: (Double?) -> Unit, modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf(heightCm?.let(::formatNumber) ?: "") }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onHeightChanged(it.toDoubleOrNull())
        },
        label = { Text("Height (cm)") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun HeightImperialInput(heightCm: Double?, onHeightChanged: (Double?) -> Unit, modifier: Modifier = Modifier) {
    val initialFeetInches = heightCm?.let(UnitConverter::cmToFeetInches)
    var feetText by remember { mutableStateOf(initialFeetInches?.first?.toString() ?: "") }
    var inchesText by remember { mutableStateOf(initialFeetInches?.second?.let(::formatNumber) ?: "") }

    fun pushChange(feet: String, inches: String) {
        val feetValue = feet.toIntOrNull()
        val inchesValue = inches.toDoubleOrNull()
        onHeightChanged(
            if (feetValue != null && inchesValue != null) {
                UnitConverter.feetInchesToCm(feetValue, inchesValue)
            } else {
                null
            },
        )
    }

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = feetText,
            onValueChange = {
                feetText = it
                pushChange(it, inchesText)
            },
            label = { Text("Feet") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = inchesText,
            onValueChange = {
                inchesText = it
                pushChange(feetText, it)
            },
            label = { Text("Inches") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun WeightMetricInput(
    label: String,
    weightKg: Double?,
    onWeightChanged: (Double?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf(weightKg?.let(::formatNumber) ?: "") }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onWeightChanged(it.toDoubleOrNull())
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun WeightImperialInput(
    label: String,
    weightKg: Double?,
    onWeightChanged: (Double?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf(weightKg?.let { formatNumber(UnitConverter.kgToLbs(it)) } ?: "") }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onWeightChanged(it.toDoubleOrNull()?.let(UnitConverter::lbsToKg))
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun AgeInput(age: Int?, onAgeChanged: (Int?) -> Unit, modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf(age?.toString() ?: "") }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onAgeChanged(it.toIntOrNull())
        },
        label = { Text("Age") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun GoalDeadlineInput(weeks: Int?, onChanged: (Int?) -> Unit, modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf(weeks?.toString() ?: "") }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onChanged(it.toIntOrNull())
        },
        label = { Text("Timeline (weeks)") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
    )
}

private fun formatNumber(value: Double): String =
    if (value == value.toInt().toDouble()) value.toInt().toString() else value.toString()
