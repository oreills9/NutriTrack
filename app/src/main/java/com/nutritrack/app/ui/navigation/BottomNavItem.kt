package com.nutritrack.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    // Routes that count as "inside" this tab, so it stays highlighted on drill-down screens
    // (e.g. Health stays selected on the weight history or new-reading screens).
    val sectionRoutes: Set<String> = setOf(route),
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Diary.route,
        label = "Diary",
        icon = Icons.Filled.RestaurantMenu,
    ),
    BottomNavItem(
        route = Screen.ActivityLog.route,
        label = "Activity",
        icon = Icons.Filled.FitnessCenter,
    ),
    BottomNavItem(
        route = Screen.BloodPressureAnalysis.route,
        label = "Health",
        icon = Icons.Filled.MonitorHeart,
        sectionRoutes = setOf(
            Screen.BloodPressureAnalysis.route,
            Screen.BloodPressureEntry.route,
            Screen.WeightHistory.route,
        ),
    ),
    BottomNavItem(
        route = Screen.WeeklySummary.route,
        label = "Summary",
        icon = Icons.Filled.BarChart,
    ),
)
