package com.nutritrack.app.ui.screens.weeklysummary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutritrack.app.data.local.entity.MealSlot
import com.nutritrack.app.domain.weight.WeightGoalProgress
import com.nutritrack.app.ui.theme.PaceGreen
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LegendItem
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.common.vicoTheme
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
import java.time.format.TextStyle as JavaTextStyle

private val WEEK_RANGE_FORMAT = DateTimeFormatter.ofPattern("MMM d")
private val ChartColors = listOf(Color(0xFF2A78D6), PaceGreen) // food = blue, activity = green

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklySummaryScreen(
    modifier: Modifier = Modifier,
    viewModel: WeeklySummaryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Summary") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            WeekNavigator(uiState = uiState, onPrevious = viewModel::goToPreviousWeek, onNext = viewModel::goToNextWeek)
            StatGrid(uiState = uiState)
            WeeklyIntakeChart(uiState = uiState)
            MealTimingBreakdown(items = uiState.mealSlotBreakdown)
            uiState.weightGoalProgress?.let { WeightGoalProgressCard(progress = it) }
            InsightsSection(insights = uiState.insights)
        }
    }
}

@Composable
private fun WeekNavigator(
    uiState: WeeklySummaryUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous week")
        }
        Text(
            "${uiState.weekStart.format(WEEK_RANGE_FORMAT)} - ${uiState.weekEnd.format(WEEK_RANGE_FORMAT)}",
            style = MaterialTheme.typography.titleMedium,
        )
        IconButton(onClick = onNext, enabled = uiState.canNavigateForward) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next week")
        }
    }
}

@Composable
private fun StatGrid(uiState: WeeklySummaryUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("Avg Daily Intake", "${uiState.averageDailyIntake.roundToInt()} kcal", Modifier.weight(1f))
            StatTile("Avg Activity Burn", "${uiState.averageActivityBurn.roundToInt()} kcal", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(
                "Weight Change",
                uiState.weightChangeKg?.let { "${"%+.1f".format(it)} kg" } ?: "-",
                Modifier.weight(1f),
            )
            StatTile("Days Logged", "${uiState.daysFullyLogged} / 7", Modifier.weight(1f))
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
private fun WeeklyIntakeChart(uiState: WeeklySummaryUiState, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(uiState.chartPoints) {
        if (uiState.chartPoints.isEmpty()) return@LaunchedEffect
        val xValues: List<Number> = uiState.chartPoints.indices.map { it }
        modelProducer.runTransaction {
            columnModel {
                series(xValues, uiState.chartPoints.map { it.intake })
                series(xValues, uiState.chartPoints.map { it.burned })
            }
        }
    }

    val legendLabelComponent = rememberTextComponent(TextStyle(vicoTheme.textColor, 12.sp))
    val target = uiState.dailyCalorieTarget
    val decorations = if (target != null) listOf(rememberTargetLine(target)) else emptyList()
    val weekStart = uiState.weekStart

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    ChartColors.map { color -> rememberLineComponent(fill = Fill(color), thickness = 16.dp) },
                ),
                mergeMode = { ColumnCartesianLayer.MergeMode.Stacked },
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                // Computed directly from x + weekStart rather than a stored per-index label list -
                // the model persists across recompositions inside modelProducer while a remembered
                // label list can momentarily go out of sync with it (e.g. on first composition,
                // before chartPoints has loaded), and Vico 3.x throws if the formatter ever returns
                // a blank string for a position it's measuring.
                valueFormatter = CartesianValueFormatter { _, x, _ ->
                    weekStart.plusDays(x.toLong()).dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault())
                },
            ),
            decorations = decorations,
            legend = rememberHorizontalLegend(
                items = {
                    add(LegendItem(ShapeComponent(Fill(ChartColors[0]), CircleShape), legendLabelComponent, "Food"))
                    add(LegendItem(ShapeComponent(Fill(ChartColors[1]), CircleShape), legendLabelComponent, "Activity"))
                },
                padding = Insets(top = 16.dp),
            ),
        ),
        modelProducer = modelProducer,
        modifier = modifier.fillMaxWidth().height(240.dp),
    )
}

@Composable
private fun rememberTargetLine(targetCalories: Int): HorizontalLine {
    val line = rememberLineComponent(fill = Fill(MaterialTheme.colorScheme.onSurface), thickness = 2.dp)
    val labelComponent = rememberTextComponent(TextStyle(vicoTheme.textColor, 11.sp))
    return remember(targetCalories) {
        HorizontalLine(
            y = { targetCalories.toDouble() },
            line = line,
            labelComponent = labelComponent,
            label = { "Target" },
        )
    }
}

@Composable
private fun MealTimingBreakdown(items: List<MealSlotBreakdownItem>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Meal Timing", style = MaterialTheme.typography.titleMedium)
        val maxCalories = items.maxOfOrNull { it.averageCalories }?.coerceAtLeast(1.0) ?: 1.0
        items.forEach { item -> MealTimingRow(item = item, maxCalories = maxCalories) }
    }
}

@Composable
private fun MealTimingRow(item: MealSlotBreakdownItem, maxCalories: Double, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(item.mealSlot.displayLabel(), style = MaterialTheme.typography.bodyMedium)
            Text(
                "${item.averageCalories.roundToInt()} kcal (${item.percentageOfIntake.roundToInt()}%)",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        val fraction = (item.averageCalories / maxCalories).toFloat().coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@Composable
private fun WeightGoalProgressCard(progress: WeightGoalProgress, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Weight Goal Progress", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                WeightStat("Start", progress.startWeightKg)
                WeightStat("Current", progress.currentWeightKg)
                WeightStat("Target", progress.targetWeightKg)
            }
            LinearProgressIndicator(
                progress = { progress.progressFraction },
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Week ${progress.currentWeek} of ${progress.totalWeeks}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun WeightStat(label: String, weightKg: Double, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text("${"%.1f".format(weightKg)} kg", style = MaterialTheme.typography.bodyMedium)
    }
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

private fun MealSlot.displayLabel(): String = when (this) {
    MealSlot.MORNING -> "Morning"
    MealSlot.AFTERNOON -> "Afternoon"
    MealSlot.EVENING -> "Evening"
    MealSlot.ACTIVITY_SNACK -> "Activity"
}
