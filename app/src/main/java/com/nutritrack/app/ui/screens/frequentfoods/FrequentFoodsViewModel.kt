package com.nutritrack.app.ui.screens.frequentfoods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.data.local.entity.FrequentFoodEntity
import com.nutritrack.app.data.repository.FrequentFoodsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TOP_FREQUENT_FOODS_LIMIT = 10

data class FrequentFoodsUiState(
    val topFoods: List<FrequentFoodEntity> = emptyList(),
)

@HiltViewModel
class FrequentFoodsViewModel @Inject constructor(
    private val frequentFoodsRepository: FrequentFoodsRepository,
) : ViewModel() {

    val uiState: StateFlow<FrequentFoodsUiState> =
        frequentFoodsRepository.observeTopFrequentFoods(TOP_FREQUENT_FOODS_LIMIT)
            .map { FrequentFoodsUiState(topFoods = it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FrequentFoodsUiState())

    fun recordLog(food: FrequentFoodEntity) {
        viewModelScope.launch { frequentFoodsRepository.recordLog(food) }
    }
}
