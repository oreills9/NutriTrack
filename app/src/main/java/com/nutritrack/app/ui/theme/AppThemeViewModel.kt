package com.nutritrack.app.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.data.prefs.AppPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppThemeViewModel @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    val selectedTheme: StateFlow<ColorTheme> = appPreferencesRepository.selectedColorThemeName
        .map { name -> ColorTheme.entries.find { it.name == name } ?: ColorTheme.DYNAMIC }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ColorTheme.DYNAMIC)

    fun selectTheme(theme: ColorTheme) {
        viewModelScope.launch { appPreferencesRepository.setSelectedColorThemeName(theme.name) }
    }
}
