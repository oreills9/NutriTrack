package com.nutritrack.app.ui.screens.fooddiary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutritrack.app.data.local.entity.FoodEntryEntity
import com.nutritrack.app.data.local.entity.MealSlot
import com.nutritrack.app.domain.calorie.CaloriePaceIndicator
import com.nutritrack.app.ui.theme.PaceAmber
import com.nutritrack.app.ui.theme.PaceGreen
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private const val SECONDS_PER_DAY = 86_400f
private val TOP_BAR_DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, MMM d")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    onAddFood: (MealSlot) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSupplements: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FoodDiaryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            DiaryTopBar(
                uiState = uiState,
                onOpenSettings = onOpenSettings,
                onOpenSupplements = onOpenSupplements,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            WeekCalendarStrip(selectedDate = uiState.selectedDate, onDateSelected = viewModel::selectDate)

            Spacer(modifier = Modifier.height(16.dp))

            CaloriePacingSection(uiState = uiState)

            Spacer(modifier = Modifier.height(12.dp))

            MealSlotSplitPillRow(uiState = uiState)

            Spacer(modifier = Modifier.height(8.dp))

            MealSlot.entries.forEach { slot ->
                MealSlotSection(
                    mealSlot = slot,
                    entries = uiState.entriesByMealSlot[slot].orEmpty(),
                    onAddFood = { onAddFood(slot) },
                )
            }

            DiaryFooter(uiState = uiState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiaryTopBar(
    uiState: FoodDiaryUiState,
    onOpenSettings: () -> Unit,
    onOpenSupplements: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TopAppBar(
            title = { Text(uiState.selectedDate.format(TOP_BAR_DATE_FORMAT)) },
            actions = {
                IconButton(onClick = onOpenSupplements) {
                    Icon(Icons.Filled.Medication, contentDescription = "Supplements")
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
            },
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            DiaryStat(label = "Consumed", value = "${uiState.totalCaloriesConsumed.roundToInt()} kcal")
            DiaryStat(
                label = "Remaining",
                value = uiState.remainingAdjustedCalories?.let { "${it.roundToInt()} kcal" } ?: "Set a target",
            )
        }
    }
}

@Composable
private fun DiaryStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun WeekCalendarStrip(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = remember { LocalDate.now() }
    val weekStart = remember(today) { today.with(DayOfWeek.MONDAY) }
    val weekDays = remember(weekStart) { (0..6).map { weekStart.plusDays(it.toLong()) } }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(weekDays, key = { it.toEpochDay() }) { date ->
            DayCell(
                date = date,
                isToday = date == today,
                isSelected = date == selectedDate,
                isFuture = date.isAfter(today),
                onClick = { onDateSelected(date) },
            )
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .width(48.dp)
            .alpha(if (isFuture) 0.4f else 1f)
            .clickable(enabled = !isFuture, onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun CaloriePacingSection(uiState: FoodDiaryUiState, modifier: Modifier = Modifier) {
    val target = uiState.dailyCalorieTarget
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Calorie Pace", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        if (target == null) {
            Text(
                "Set a daily target in your profile to see pacing.",
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            val isToday = uiState.selectedDate == LocalDate.now()
            // Net of activity burned, so the bar actually moves when an activity is logged.
            val netConsumed = uiState.totalCaloriesConsumed - uiState.totalCaloriesBurned
            val progress = (netConsumed / target).toFloat().coerceIn(0f, 1f)
            val expectedProgress = (LocalTime.now().toSecondOfDay() / SECONDS_PER_DAY).coerceIn(0f, 1f)
            val barColor = if (isToday && uiState.paceIndicator == CaloriePaceIndicator.OVER_PACE) {
                PaceAmber
            } else {
                PaceGreen
            }
            CaloriePacingBar(
                progress = progress,
                expectedProgress = expectedProgress,
                showMarker = isToday,
                barColor = barColor,
            )
        }
    }
}

@Composable
private fun CaloriePacingBar(
    progress: Float,
    expectedProgress: Float,
    showMarker: Boolean,
    barColor: Color,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth().height(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .clip(RoundedCornerShape(8.dp))
                .background(barColor),
        )
        if (showMarker) {
            Box(
                modifier = Modifier
                    .offset(x = maxWidth * expectedProgress - 1.dp)
                    .fillMaxHeight()
                    .width(2.dp)
                    .background(MaterialTheme.colorScheme.onSurface),
            )
        }
    }
}

@Composable
private fun MealSlotSplitPillRow(uiState: FoodDiaryUiState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MealSlot.entries.forEach { slot ->
            val calories = uiState.entriesByMealSlot[slot].orEmpty().sumOf { it.calories }
            SplitPill(label = slot.displayLabel(), calories = calories)
        }
    }
}

@Composable
private fun SplitPill(label: String, calories: Double, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.large,
        modifier = modifier,
    ) {
        Text(
            "$label: ${calories.roundToInt()}",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun MealSlotSection(
    mealSlot: MealSlot,
    entries: List<FoodEntryEntity>,
    onAddFood: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable(mealSlot) { mutableStateOf(true) }
    val subtotal = entries.sumOf { it.calories }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(mealSlot.displayLabel(), style = MaterialTheme.typography.titleMedium)
                Text("${subtotal.roundToInt()} kcal", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onAddFood) {
                Icon(Icons.Filled.Add, contentDescription = "Add food to ${mealSlot.displayLabel()}")
            }
        }
        if (expanded) {
            if (entries.isEmpty()) {
                Text(
                    "No foods logged yet",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            } else {
                entries.forEach { entry -> FoodEntryRow(entry) }
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun FoodEntryRow(entry: FoodEntryEntity, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(entry.foodName, style = MaterialTheme.typography.bodyMedium)
        Text("${entry.calories.roundToInt()} kcal", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DiaryFooter(uiState: FoodDiaryUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        FooterStatRow("Total Intake", "${uiState.totalCaloriesConsumed.roundToInt()} kcal")
        FooterStatRow("Activity Burned", "${uiState.totalCaloriesBurned.roundToInt()} kcal")
        FooterStatRow("Net Intake", uiState.netCalories?.let { "${it.roundToInt()} kcal" } ?: "-")
        FooterStatRow(
            "Remaining",
            uiState.remainingAdjustedCalories?.let { "${it.roundToInt()} kcal" } ?: "-",
        )
    }
}

@Composable
private fun FooterStatRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

private fun MealSlot.displayLabel(): String = when (this) {
    MealSlot.MORNING -> "Morning"
    MealSlot.AFTERNOON -> "Afternoon"
    MealSlot.EVENING -> "Evening"
    MealSlot.ACTIVITY_SNACK -> "Activity"
}
