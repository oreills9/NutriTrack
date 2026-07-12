package com.nutritrack.app.ui.screens.supplements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.data.local.entity.SupplementEntryEntity
import com.nutritrack.app.data.repository.SupplementsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class SupplementsUiState(
    val checklist: List<SupplementEntryEntity> = emptyList(),
)

@HiltViewModel
class SupplementsViewModel @Inject constructor(
    private val supplementsRepository: SupplementsRepository,
) : ViewModel() {

    val uiState: StateFlow<SupplementsUiState> = supplementsRepository.observeChecklist()
        .map { SupplementsUiState(checklist = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SupplementsUiState())

    fun markTaken(id: Long, taken: Boolean) {
        viewModelScope.launch { supplementsRepository.setTaken(id, taken) }
    }

    fun addSupplement(name: String, dosageNotes: String?, timeOfDay: LocalTime) {
        viewModelScope.launch {
            supplementsRepository.addSupplement(
                SupplementEntryEntity(name = name, dosageNotes = dosageNotes, timeOfDay = timeOfDay),
            )
        }
    }

    fun deleteSupplement(entry: SupplementEntryEntity) {
        viewModelScope.launch { supplementsRepository.deleteSupplement(entry) }
    }
}
