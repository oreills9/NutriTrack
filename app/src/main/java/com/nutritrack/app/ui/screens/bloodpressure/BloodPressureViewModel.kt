package com.nutritrack.app.ui.screens.bloodpressure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.data.local.entity.BloodPressureEntryEntity
import com.nutritrack.app.data.local.entity.TimeOfDay
import com.nutritrack.app.data.repository.BloodPressureRepository
import com.nutritrack.app.domain.bloodpressure.BloodPressureAnalyzer
import com.nutritrack.app.domain.bloodpressure.BloodPressureCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BloodPressureUiState(
    val latestReading: BloodPressureEntryEntity? = null,
    val systolic: Int? = null,
    val diastolic: Int? = null,
    val heartRateBpm: Int? = null,
    val timeOfDay: TimeOfDay = TimeOfDay.MORNING,
    val note: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
) {
    val liveClassification: BloodPressureCategory?
        get() {
            val sys = systolic ?: return null
            val dia = diastolic ?: return null
            return BloodPressureAnalyzer.classify(sys, dia)
        }

    val canSave: Boolean
        get() = systolic != null && diastolic != null && heartRateBpm != null
}

@HiltViewModel
class BloodPressureViewModel @Inject constructor(
    private val bloodPressureRepository: BloodPressureRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BloodPressureUiState())
    val uiState: StateFlow<BloodPressureUiState> = _uiState.asStateFlow()

    init {
        bloodPressureRepository.observeLatestReading()
            .onEach { latest -> _uiState.update { it.copy(latestReading = latest) } }
            .launchIn(viewModelScope)
    }

    fun updateSystolic(value: Int?) = _uiState.update { it.copy(systolic = value) }

    fun updateDiastolic(value: Int?) = _uiState.update { it.copy(diastolic = value) }

    fun updateHeartRate(value: Int?) = _uiState.update { it.copy(heartRateBpm = value) }

    fun selectTimeOfDay(timeOfDay: TimeOfDay) = _uiState.update { it.copy(timeOfDay = timeOfDay) }

    fun updateNote(note: String) = _uiState.update { it.copy(note = note) }

    fun saveReading() {
        val state = _uiState.value
        val systolic = state.systolic ?: return
        val diastolic = state.diastolic ?: return
        val heartRate = state.heartRateBpm ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            bloodPressureRepository.logReading(
                BloodPressureEntryEntity(
                    date = LocalDate.now(),
                    systolic = systolic,
                    diastolic = diastolic,
                    heartRateBpm = heartRate,
                    timeOfDay = state.timeOfDay,
                    note = state.note.trim().ifBlank { null },
                ),
            )
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }
}
