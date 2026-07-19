package com.nutritrack.app.ui.screens.bloodpressure

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.data.export.CsvFileWriter
import com.nutritrack.app.data.local.entity.BloodPressureEntryEntity
import com.nutritrack.app.data.repository.BloodPressureRepository
import com.nutritrack.app.domain.bloodpressure.BloodPressureAnalyzer
import com.nutritrack.app.domain.bloodpressure.BloodPressureAverages
import com.nutritrack.app.domain.bloodpressure.BloodPressureCsvExporter
import com.nutritrack.app.domain.bloodpressure.BloodPressureInsightGenerator
import com.nutritrack.app.domain.bloodpressure.BloodPressureTimeRange
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

data class BloodPressureAnalysisUiState(
    val selectedRange: BloodPressureTimeRange = BloodPressureTimeRange.FOUR_WEEKS,
    val allReadings: List<BloodPressureEntryEntity> = emptyList(),
    // Oldest-first, filtered to selectedRange - used for the chart and insights.
    val rangeReadings: List<BloodPressureEntryEntity> = emptyList(),
    val averages: BloodPressureAverages? = null,
    val highestReading: BloodPressureEntryEntity? = null,
    val insights: List<String> = emptyList(),
    val isExporting: Boolean = false,
    val exportMessage: String? = null,
) {
    // Most recent first, for the history list.
    val historyReadings: List<BloodPressureEntryEntity>
        get() = rangeReadings.asReversed()
}

@HiltViewModel
class BloodPressureAnalysisViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bloodPressureRepository: BloodPressureRepository,
) : ViewModel() {

    private val selectedRange = MutableStateFlow(BloodPressureTimeRange.FOUR_WEEKS)
    private val _uiState = MutableStateFlow(BloodPressureAnalysisUiState())
    val uiState: StateFlow<BloodPressureAnalysisUiState> = _uiState.asStateFlow()

    init {
        combine(
            bloodPressureRepository.observeAllReadings(),
            selectedRange,
        ) { readings, range -> readings to range }
            .onEach { (readings, range) ->
                val filtered = BloodPressureAnalyzer.filterForRange(readings, range)
                _uiState.update {
                    it.copy(
                        selectedRange = range,
                        allReadings = readings,
                        rangeReadings = filtered,
                        averages = BloodPressureAnalyzer.calculateAverages(filtered),
                        highestReading = filtered.maxByOrNull { reading -> reading.systolic },
                        insights = BloodPressureInsightGenerator.generate(filtered),
                    )
                }
            }.launchIn(viewModelScope)
    }

    fun selectRange(range: BloodPressureTimeRange) {
        selectedRange.value = range
    }

    // Exports every logged reading, independent of the currently selected tab.
    fun exportCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val csv = BloodPressureCsvExporter.toCsv(_uiState.value.allReadings)
            val fileName = "blood_pressure_export_${LocalDate.now()}.csv"
            val uri = withContext(Dispatchers.IO) { CsvFileWriter.write(context, fileName, csv) }
            _uiState.update {
                it.copy(isExporting = false, exportMessage = if (uri != null) "Exported to Downloads" else "Export failed")
            }
        }
    }

    fun clearExportMessage() = _uiState.update { it.copy(exportMessage = null) }
}
