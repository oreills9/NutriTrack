package com.nutritrack.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.data.prefs.AppPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppStartViewModel @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    // Null while undetermined; the app shows a loading state until this resolves.
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val hasCompletedOnboarding = appPreferencesRepository.hasCompletedOnboarding.first()
            _startDestination.value = if (hasCompletedOnboarding) {
                Screen.Diary.route
            } else {
                Screen.ProfileSetup.route
            }
        }
    }
}
