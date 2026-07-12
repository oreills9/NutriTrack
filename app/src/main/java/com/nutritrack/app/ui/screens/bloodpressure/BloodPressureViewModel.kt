package com.nutritrack.app.ui.screens.bloodpressure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.data.local.entity.BloodPressureEntryEntity
import com.nutritrack.app.data.local.entity.TimeOfDay
import com.nutritrack.app.data.repository.BloodPressureRepository
import com.nutritrack.app.domain.bloodpressure.BloodPressureAnalyzer
import com.nutritrack.app.domain.bloodpressure.BloodPressureAverages
import com.nutritrack.app.domain.bloodpressure.BloodPressureCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BloodPressureUiState(
    val readings: List<BloodPressureEntryEntity> = emptyList(),
    val eightWeekAverages: BloodPressureAverages? = null,
)

@HiltViewModel
class BloodPressureViewModel @Inject constructor(
    private val bloodPressureRepository: BloodPressureRepository,
) : ViewModel() {

    val uiState: StateFlow<BloodPressureUiState> = bloodPressureRepository.observeAllReadings()
        .map { readings ->
            BloodPressureUiState(
                readings = readings,
                eightWeekAverages = BloodPressureAnalyzer.calculateAverages(readings),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BloodPressureUiState())

    fun classify(systolic: Int, diastolic: Int): BloodPressureCategory =
        BloodPressureAnalyzer.classify(systolic, diastolic)

    fun logReading(systolic: Int, diastolic: Int, heartRateBpm: Int, timeOfDay: TimeOfDay, note: String? = null) {
        viewModelScope.launch {
            bloodPressureRepository.logReading(
                BloodPressureEntryEntity(
                    date = LocalDate.now(),
                    systolic = systolic,
                    diastolic = diastolic,
                    heartRateBpm = heartRateBpm,
                    timeOfDay = timeOfDay,
                    note = note,
                ),
            )
        }
    }

    fun updateReading(entry: BloodPressureEntryEntity) {
        viewModelScope.launch { bloodPressureRepository.updateReading(entry) }
    }

    fun deleteReading(entry: BloodPressureEntryEntity) {
        viewModelScope.launch { bloodPressureRepository.deleteReading(entry) }
    }
}
