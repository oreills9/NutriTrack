package com.nutritrack.app.ui.screens.bloodpressure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutritrack.app.data.local.entity.BloodPressureEntryEntity
import com.nutritrack.app.data.local.entity.TimeOfDay
import com.nutritrack.app.domain.bloodpressure.BloodPressureAnalyzer
import com.nutritrack.app.domain.bloodpressure.BloodPressureCategory
import com.nutritrack.app.domain.bloodpressure.BloodPressureTimeRange
import com.nutritrack.app.ui.screens.weighthistory.LogWeightBottomSheet
import com.nutritrack.app.ui.screens.weighthistory.WeightHistoryViewModel
import com.nutritrack.app.ui.theme.BpElevatedAmber
import com.nutritrack.app.ui.theme.BpNormalGreen
import com.nutritrack.app.ui.theme.BpStage1Orange
import com.nutritrack.app.ui.theme.BpStage2Red
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalBox
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LegendItem
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.common.vicoTheme
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val HISTORY_DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, MMM d")
private val SystolicColor = BpStage2Red
private val DiastolicColor = Color(0xFF2A78D6)
private const val NORMAL_BAND_TOP = 120.0

private val TIME_RANGE_OPTIONS = listOf(
    BloodPressureTimeRange.FOUR_WEEKS to "4 Weeks",
    BloodPressureTimeRange.EIGHT_WEEKS to "8 Weeks",
    BloodPressureTimeRange.THREE_MONTHS to "3 Months",
    BloodPressureTimeRange.ALL_TIME to "All Time",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodPressureAnalysisScreen(
    onLogNewReading: () -> Unit,
    onViewWeightHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BloodPressureAnalysisViewModel = hiltViewModel(),
    weightHistoryViewModel: WeightHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var fabMenuExpanded by remember { mutableStateOf(false) }
    var showLogWeightSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Blood Pressure Analysis") },
                    actions = {
                        IconButton(onClick = viewModel::exportCsv, enabled = !uiState.isExporting && uiState.allReadings.isNotEmpty()) {
                            if (uiState.isExporting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Filled.Download, contentDescription = "Export all readings as CSV")
                            }
                        }
                    },
                )
                PrimaryTabRow(selectedTabIndex = uiState.selectedRange.ordinal) {
                    TIME_RANGE_OPTIONS.forEach { (range, label) ->
                        Tab(
                            selected = uiState.selectedRange == range,
                            onClick = { viewModel.selectRange(range) },
                            text = { Text(label) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { fabMenuExpanded = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Log a new entry")
                }
                DropdownMenu(expanded = fabMenuExpanded, onDismissRequest = { fabMenuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Log Blood Pressure") },
                        onClick = {
                            fabMenuExpanded = false
                            onLogNewReading()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Log Weight") },
                        onClick = {
                            fabMenuExpanded = false
                            showLogWeightSheet = true
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            StatGrid(uiState = uiState)

            if (uiState.rangeReadings.isEmpty()) {
                Text(
                    "No readings logged in this period.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                BloodPressureLineChart(readings = uiState.rangeReadings)
                InsightsSection(insights = uiState.insights)
                HistorySection(readings = uiState.historyReadings)
            }

            uiState.exportMessage?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }

            TextButton(onClick = onViewWeightHistory) { Text("View weight history") }
        }
    }

    if (showLogWeightSheet) {
        LogWeightBottomSheet(
            onDismiss = { showLogWeightSheet = false },
            onSave = { weightKg, note ->
                weightHistoryViewModel.logWeight(weightKg, note)
                showLogWeightSheet = false
            },
        )
    }
}

@Composable
private fun StatGrid(uiState: BloodPressureAnalysisUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(
                "Avg BP",
                uiState.averages?.let { "${it.averageSystolic.roundToInt()}/${it.averageDiastolic.roundToInt()}" } ?: "-",
                Modifier.weight(1f),
            )
            StatTile(
                "Avg Heart Rate",
                uiState.averages?.let { "${it.averageHeartRateBpm.roundToInt()} bpm" } ?: "-",
                Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(
                "Highest Reading",
                uiState.highestReading?.let { "${it.systolic}/${it.diastolic}" } ?: "-",
                Modifier.weight(1f),
            )
            StatTile("Readings Logged", "${uiState.rangeReadings.size}", Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun BloodPressureLineChart(readings: List<BloodPressureEntryEntity>, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(readings) {
        val xValues: List<Number> = readings.indices.map { it }
        modelProducer.runTransaction {
            lineModel {
                series(xValues, readings.map { it.systolic })
                series(xValues, readings.map { it.diastolic })
            }
        }
    }

    val legendLabelComponent = rememberTextComponent(TextStyle(vicoTheme.textColor, 12.sp))
    val normalBandLabelComponent = rememberTextComponent(TextStyle(vicoTheme.textColor, 10.sp))
    val normalBandShape = remember { ShapeComponent(fill = Fill(BpNormalGreen.copy(alpha = 0.12f))) }
    val decorations = remember(normalBandShape, normalBandLabelComponent) {
        listOf(
            HorizontalBox(
                y = { 0.0..NORMAL_BAND_TOP },
                box = normalBandShape,
                labelComponent = normalBandLabelComponent,
                label = { "Normal (<120)" },
            ),
        )
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(fill = LineCartesianLayer.LineFill.single(Fill(SystolicColor))),
                    LineCartesianLayer.rememberLine(fill = LineCartesianLayer.LineFill.single(Fill(DiastolicColor))),
                ),
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                // Formats directly from the readings list captured in this composable's scope,
                // clamped to a valid index, rather than a stored per-index label list read back
                // out of the model's extraStore. The model persists across recompositions inside
                // modelProducer while a remembered label list can momentarily go out of sync with
                // it, and Vico 3.x throws if the formatter ever returns a blank string for a
                // position it's measuring.
                valueFormatter = CartesianValueFormatter { _, x, _ ->
                    val index = x.toInt().coerceIn(readings.indices)
                    readings[index].date.format(HISTORY_DATE_FORMAT)
                },
            ),
            decorations = decorations,
            legend = rememberHorizontalLegend(
                items = {
                    add(LegendItem(ShapeComponent(Fill(SystolicColor), CircleShape), legendLabelComponent, "Systolic"))
                    add(LegendItem(ShapeComponent(Fill(DiastolicColor), CircleShape), legendLabelComponent, "Diastolic"))
                },
                padding = Insets(top = 16.dp),
            ),
        ),
        modelProducer = modelProducer,
        modifier = modifier.fillMaxWidth().height(240.dp),
    )
}

@Composable
private fun InsightsSection(insights: List<String>, modifier: Modifier = Modifier) {
    if (insights.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Insights", style = MaterialTheme.typography.titleMedium)
        insights.forEach { insight ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(insight, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
            }
        }
    }
}

@Composable
private fun HistorySection(readings: List<BloodPressureEntryEntity>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Reading History", style = MaterialTheme.typography.titleMedium)
        readings.forEach { reading -> HistoryReadingRow(reading = reading) }
    }
}

@Composable
private fun HistoryReadingRow(reading: BloodPressureEntryEntity, modifier: Modifier = Modifier) {
    val category = BloodPressureAnalyzer.classify(reading.systolic, reading.diastolic)
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(reading.date.format(HISTORY_DATE_FORMAT), style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${reading.timeOfDay.displayLabel()} - HR ${reading.heartRateBpm}",
                    style = MaterialTheme.typography.bodySmall,
                )
                reading.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${reading.systolic}/${reading.diastolic}", style = MaterialTheme.typography.titleMedium)
                ClassificationBadge(category)
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
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
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
