package com.nutritrack.app.ui.screens.weighthistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.data.local.entity.WeightEntryEntity
import com.nutritrack.app.data.repository.WeightHistoryRepository
import com.nutritrack.app.domain.weight.WeightTrendCalculator
import com.nutritrack.app.domain.weight.WeightTrendPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class WeightHistoryUiState(
    val trendPoints: List<WeightTrendPoint> = emptyList(),
    val totalWeightLostKg: Double = 0.0,
)

@HiltViewModel
class WeightHistoryViewModel @Inject constructor(
    private val weightHistoryRepository: WeightHistoryRepository,
) : ViewModel() {

    val uiState: StateFlow<WeightHistoryUiState> = weightHistoryRepository.observeAllEntries()
        .map { entries -> entries.sortedBy { it.date } }
        .map { entriesAscending ->
            WeightHistoryUiState(
                trendPoints = WeightTrendCalculator.calculateRollingAverages(entriesAscending),
                totalWeightLostKg = WeightTrendCalculator.calculateTotalWeightLost(entriesAscending),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeightHistoryUiState())

    fun logWeight(weightKg: Double, note: String? = null) {
        viewModelScope.launch {
            weightHistoryRepository.logWeight(WeightEntryEntity(date = LocalDate.now(), weightKg = weightKg, note = note))
        }
    }

    fun updateEntry(entry: WeightEntryEntity) {
        viewModelScope.launch { weightHistoryRepository.updateEntry(entry) }
    }

    fun deleteEntry(entry: WeightEntryEntity) {
        viewModelScope.launch { weightHistoryRepository.deleteEntry(entry) }
    }
}
