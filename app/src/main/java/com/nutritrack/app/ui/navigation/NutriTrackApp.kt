package com.nutritrack.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun NutriTrackApp(
    modifier: Modifier = Modifier,
    pendingRoute: String? = null,
    onPendingRouteConsumed: () -> Unit = {},
) {
    val appStartViewModel: AppStartViewModel = hiltViewModel()
    val startDestination by appStartViewModel.startDestination.collectAsStateWithLifecycle()

    when (val destination = startDestination) {
        null -> LoadingScreen(modifier)
        else -> NutriTrackScaffold(
            startDestination = destination,
            pendingRoute = pendingRoute,
            onPendingRouteConsumed = onPendingRouteConsumed,
            modifier = modifier,
        )
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun NutriTrackScaffold(
    startDestination: String,
    pendingRoute: String?,
    onPendingRouteConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = bottomNavItems.any { it.sectionRoutes.contains(currentRoute) }

    // Only honour a notification-launched destination once onboarding is already done -
    // otherwise let the normal profile_setup flow run and drop the pending navigation.
    LaunchedEffect(pendingRoute) {
        if (pendingRoute != null && startDestination == Screen.Diary.route) {
            navController.navigate(pendingRoute)
            onPendingRouteConsumed()
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NutriTrackBottomBar(navController = navController, currentRoute = currentRoute)
            }
        },
    ) { innerPadding ->
        NutriTrackNavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun NutriTrackBottomBar(navController: NavController, currentRoute: String?) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = item.sectionRoutes.contains(currentRoute),
                onClick = {
                    navController.navigate(item.route) {
                        // Pop to Diary specifically, not graph.findStartDestination(): the graph's
                        // static start node can be profile_setup, which is removed from the back
                        // stack (inclusive) once onboarding completes, so it's not a safe pop target.
                        popUpTo(Screen.Diary.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}
