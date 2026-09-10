package com.cardwise.app.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cardwise.app.domain.insights.InsightsEngine
import com.cardwise.app.domain.insights.InsightsSummary
import com.cardwise.app.domain.repository.PaymentHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class InsightsViewModel(
    repository: PaymentHistoryRepository,
    engine: InsightsEngine = InsightsEngine()
) : ViewModel() {
    val uiState: StateFlow<InsightsUiState> = repository.observeEntries()
        .map { entries ->
            val summary = engine.summarize(entries)
            if (summary.hasEnoughHistory) InsightsUiState.Content(summary) else InsightsUiState.Empty
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState.Empty)

    companion object {
        fun factory(repository: PaymentHistoryRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    InsightsViewModel(repository) as T
            }
    }
}

sealed interface InsightsUiState {
    data object Empty : InsightsUiState
    data class Content(val summary: InsightsSummary) : InsightsUiState
}
