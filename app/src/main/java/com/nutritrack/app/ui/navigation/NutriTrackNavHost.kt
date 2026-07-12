package com.nutritrack.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nutritrack.app.ui.screens.activitylog.ActivityLogScreen
import com.nutritrack.app.ui.screens.addfood.AddFoodScreen
import com.nutritrack.app.ui.screens.bloodpressure.BloodPressureAnalysisScreen
import com.nutritrack.app.ui.screens.bloodpressure.BloodPressureEntryScreen
import com.nutritrack.app.ui.screens.fooddiary.DiaryScreen
import com.nutritrack.app.ui.screens.profilesetup.ProfileSetupScreen
import com.nutritrack.app.ui.screens.settings.SettingsScreen
import com.nutritrack.app.ui.screens.supplements.SupplementsScreen
import com.nutritrack.app.ui.screens.weeklysummary.WeeklySummaryScreen
import com.nutritrack.app.ui.screens.weighthistory.WeightHistoryScreen

@Composable
fun NutriTrackNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                onSetupComplete = {
                    navController.navigate(Screen.Diary.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Diary.route) {
            DiaryScreen(
                onAddFood = { mealSlot -> navController.navigate(Screen.AddFood.createRoute(mealSlot)) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                onOpenSupplements = { navController.navigate(Screen.Supplements.route) },
            )
        }
        composable(
            route = Screen.AddFood.route,
            arguments = listOf(navArgument(Screen.AddFood.ARG_MEAL_SLOT) { type = NavType.StringType }),
        ) {
            // AddFoodViewModel reads mealSlot from its Hilt-provided SavedStateHandle.
            AddFoodScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.ActivityLog.route) {
            ActivityLogScreen()
        }
        composable(Screen.BloodPressureEntry.route) {
            BloodPressureEntryScreen(
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                onViewAnalysis = { navController.navigate(Screen.BloodPressureAnalysis.route) },
            )
        }
        composable(Screen.BloodPressureAnalysis.route) {
            BloodPressureAnalysisScreen(
                onLogNewReading = { navController.navigate(Screen.BloodPressureEntry.route) },
                onViewWeightHistory = { navController.navigate(Screen.WeightHistory.route) },
            )
        }
        composable(Screen.WeeklySummary.route) {
            WeeklySummaryScreen()
        }
        composable(Screen.Supplements.route) {
            SupplementsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.WeightHistory.route) {
            WeightHistoryScreen(onBack = { navController.popBackStack() })
        }
    }
}
