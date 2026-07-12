package com.nutritrack.app.ui.screens.bloodpressure

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutritrack.app.data.local.entity.BloodPressureEntryEntity
import com.nutritrack.app.data.local.entity.TimeOfDay
import com.nutritrack.app.domain.bloodpressure.BloodPressureAnalyzer
import com.nutritrack.app.domain.bloodpressure.BloodPressureCategory
import com.nutritrack.app.ui.theme.BpElevatedAmber
import com.nutritrack.app.ui.theme.BpNormalGreen
import com.nutritrack.app.ui.theme.BpStage1Orange
import com.nutritrack.app.ui.theme.BpStage2Red
import java.time.format.DateTimeFormatter

private val LAST_READING_DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, MMM d")

private data class BpRangeInfo(val category: BloodPressureCategory, val rangeText: String)

private val BP_REFERENCE_RANGES = listOf(
    BpRangeInfo(BloodPressureCategory.NORMAL, "Below 120/80"),
    BpRangeInfo(BloodPressureCategory.ELEVATED, "120-129 / below 80"),
    BpRangeInfo(BloodPressureCategory.HIGH_STAGE_1, "130-139 / 80-89"),
    BpRangeInfo(BloodPressureCategory.HIGH_STAGE_2, "140+ / 90+"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodPressureEntryScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    onViewAnalysis: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BloodPressureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Log Blood Pressure") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onViewAnalysis) {
                        Icon(Icons.Filled.Analytics, contentDescription = "View analysis")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            uiState.latestReading?.let { LastReadingBanner(reading = it) }

            EntryForm(uiState = uiState, viewModel = viewModel)

            uiState.liveClassification?.let { category ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Classification: ", style = MaterialTheme.typography.bodyMedium)
                    ClassificationBadge(category)
                }
            }

            Button(
                onClick = viewModel::saveReading,
                enabled = uiState.canSave && !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save")
            }

            ReferenceGuide()
        }
    }
}

@Composable
private fun LastReadingBanner(reading: BloodPressureEntryEntity, modifier: Modifier = Modifier) {
    val category = BloodPressureAnalyzer.classify(reading.systolic, reading.diastolic)
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Last Reading", style = MaterialTheme.typography.labelLarge)
            Text(reading.date.format(LAST_READING_DATE_FORMAT), style = MaterialTheme.typography.bodySmall)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${reading.systolic}/${reading.diastolic} mmHg", style = MaterialTheme.typography.headlineSmall)
                Text("HR ${reading.heartRateBpm}", style = MaterialTheme.typography.bodyMedium)
            }
            ClassificationBadge(category)
        }
    }
}

@Composable
private fun EntryForm(uiState: BloodPressureUiState, viewModel: BloodPressureViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(
                label = "Systolic",
                value = uiState.systolic,
                onChange = viewModel::updateSystolic,
                modifier = Modifier.weight(1f),
            )
            NumberField(
                label = "Diastolic",
                value = uiState.diastolic,
                onChange = viewModel::updateDiastolic,
                modifier = Modifier.weight(1f),
            )
        }
        NumberField(
            label = "Heart Rate (BPM)",
            value = uiState.heartRateBpm,
            onChange = viewModel::updateHeartRate,
            modifier = Modifier.fillMaxWidth(),
        )

        TimeOfDayToggle(selected = uiState.timeOfDay, onSelected = viewModel::selectTimeOfDay)

        OutlinedTextField(
            value = uiState.note,
            onValueChange = viewModel::updateNote,
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun NumberField(label: String, value: Int?, onChange: (Int?) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value?.toString() ?: "",
        onValueChange = { onChange(it.toIntOrNull()) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}

@Composable
private fun TimeOfDayToggle(selected: TimeOfDay, onSelected: (TimeOfDay) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Time of Day", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TimeOfDay.entries.forEach { timeOfDay ->
                if (timeOfDay == selected) {
                    Button(onClick = { onSelected(timeOfDay) }, modifier = Modifier.weight(1f)) {
                        Text(timeOfDay.displayLabel())
                    }
                } else {
                    OutlinedButton(onClick = { onSelected(timeOfDay) }, modifier = Modifier.weight(1f)) {
                        Text(timeOfDay.displayLabel())
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassificationBadge(category: BloodPressureCategory, modifier: Modifier = Modifier) {
    Surface(
        color = category.color(),
        contentColor = Color.White,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Text(
            category.displayLabel(),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun ReferenceGuide(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Reference Guide", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        BP_REFERENCE_RANGES.forEach { info ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color = info.category.color(), shape = CircleShape),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    info.category.displayLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(info.rangeText, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun BloodPressureCategory.displayLabel(): String = when (this) {
    BloodPressureCategory.NORMAL -> "Normal"
    BloodPressureCategory.ELEVATED -> "Elevated"
    BloodPressureCategory.HIGH_STAGE_1 -> "High Stage 1"
    BloodPressureCategory.HIGH_STAGE_2 -> "High Stage 2"
}

private fun BloodPressureCategory.color(): Color = when (this) {
    BloodPressureCategory.NORMAL -> BpNormalGreen
    BloodPressureCategory.ELEVATED -> BpElevatedAmber
    BloodPressureCategory.HIGH_STAGE_1 -> BpStage1Orange
    BloodPressureCategory.HIGH_STAGE_2 -> BpStage2Red
}

private fun TimeOfDay.displayLabel(): String = when (this) {
    TimeOfDay.MORNING -> "Morning"
    TimeOfDay.EVENING -> "Evening"
}
