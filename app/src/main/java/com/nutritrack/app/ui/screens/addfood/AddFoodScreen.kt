package com.nutritrack.app.ui.screens.addfood

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddFoodViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Add Food") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
                PrimaryTabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
                    Tab(
                        selected = uiState.selectedTab == AddFoodTab.SEARCH,
                        onClick = { viewModel.selectTab(AddFoodTab.SEARCH) },
                        text = { Text("Search") },
                    )
                    Tab(
                        selected = uiState.selectedTab == AddFoodTab.SCAN,
                        onClick = { viewModel.selectTab(AddFoodTab.SCAN) },
                        text = { Text("Scan") },
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (uiState.selectedTab) {
                AddFoodTab.SEARCH -> SearchTabContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                )
                AddFoodTab.SCAN -> ScanTabHost(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun ScanTabHost(uiState: AddFoodUiState, viewModel: AddFoodViewModel, modifier: Modifier = Modifier) {
    val selectedFood = uiState.selectedFood
    if (selectedFood != null) {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            SelectedFoodDetail(
                food = selectedFood,
                portionGrams = uiState.portionGrams,
                caloriesForPortion = uiState.caloriesForPortion,
                mealSlot = uiState.mealSlot,
                isSaving = uiState.isSaving,
                onPortionDelta = viewModel::adjustPortion,
                onPortionPreset = viewModel::setPortionGrams,
                onMealSlotSelected = viewModel::selectMealSlot,
                onLog = viewModel::logSelectedFood,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = viewModel::clearSelectedFood, modifier = Modifier.fillMaxWidth()) {
                Text("Scan Again")
            }
        }
    } else {
        Column(modifier = modifier) {
            uiState.scanErrorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
            ScanTabContent(onBarcodeScanned = viewModel::onBarcodeScanned, modifier = Modifier.weight(1f))
        }
    }
}
