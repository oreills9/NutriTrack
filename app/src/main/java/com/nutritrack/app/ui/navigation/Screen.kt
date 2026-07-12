package com.nutritrack.app.ui.navigation

import com.nutritrack.app.data.local.entity.MealSlot

sealed class Screen(val route: String) {
    data object ProfileSetup : Screen("profile_setup")
    data object Diary : Screen("diary")
    data object AddFood : Screen("add_food/{mealSlot}") {
        const val ARG_MEAL_SLOT = "mealSlot"
        fun createRoute(mealSlot: MealSlot): String = "add_food/${mealSlot.name}"
    }
    data object ActivityLog : Screen("activity_log")
    data object BloodPressureEntry : Screen("blood_pressure_entry")
    data object BloodPressureAnalysis : Screen("blood_pressure_analysis")
    data object WeeklySummary : Screen("weekly_summary")
    data object Supplements : Screen("supplements")
    data object Settings : Screen("settings")
    data object WeightHistory : Screen("weight_history")
}
