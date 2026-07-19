package com.nutritrack.app.ui.screens.supplements

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import com.nutritrack.app.data.local.entity.SupplementEntryEntity
import com.nutritrack.app.data.prefs.AppPreferencesRepository
import com.nutritrack.app.data.repository.SupplementsRepository
import com.nutritrack.app.notifications.SupplementReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class SupplementsUiState(
    val checklist: List<SupplementEntryEntity> = emptyList(),
) {
    val allTakenToday: Boolean
        get() = checklist.isNotEmpty() && checklist.all { it.takenToday }
}

@HiltViewModel
class SupplementsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supplementsRepository: SupplementsRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<SupplementsUiState> = supplementsRepository.observeChecklist()
        .map { SupplementsUiState(checklist = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SupplementsUiState())

    fun markTaken(id: Long, taken: Boolean) {
        viewModelScope.launch { supplementsRepository.setTaken(id, taken) }
    }

    fun addSupplement(name: String, dosageNotes: String?, timeOfDay: LocalTime) {
        viewModelScope.launch {
            val id = supplementsRepository.addSupplement(
                SupplementEntryEntity(name = name, dosageNotes = dosageNotes, timeOfDay = timeOfDay),
            )
            if (appPreferencesRepository.supplementReminderEnabled.first()) {
                SupplementReminderScheduler.schedule(context, id, timeOfDay, ExistingPeriodicWorkPolicy.REPLACE)
            }
        }
    }

    fun deleteSupplement(entry: SupplementEntryEntity) {
        viewModelScope.launch {
            supplementsRepository.deleteSupplement(entry)
            SupplementReminderScheduler.cancel(context, entry.id)
        }
    }
}
