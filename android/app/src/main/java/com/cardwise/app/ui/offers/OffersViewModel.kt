package com.cardwise.app.ui.offers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cardwise.app.domain.offers.Offer
import com.cardwise.app.domain.offers.OfferCatalogRepository
import com.cardwise.app.domain.resource.ResourceSyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface OffersUiState {
    data object Loading : OffersUiState
    data class Content(
        val offers: List<Offer>,
        val filter: String = "All",
        val refreshError: String? = null
    ) : OffersUiState
    data class Empty(val refreshError: String? = null) : OffersUiState
    data class Unavailable(val message: String) : OffersUiState
}

class OffersViewModel(
    private val repository: OfferCatalogRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<OffersUiState>(OffersUiState.Loading)
    val uiState: StateFlow<OffersUiState> = _uiState.asStateFlow()

    init { load() }

    fun setFilter(filter: String) {
        _uiState.update { current ->
            if (current is OffersUiState.Content) current.copy(filter = filter) else current
        }
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching { repository.refresh() }
                .onSuccess { sync -> applyOffers(repository.offers.first(), syncFailureMessage(sync)) }
                .onFailure { error ->
                    val cached = repository.loadCached()
                    if (cached.isEmpty()) _uiState.value = OffersUiState.Unavailable(error.message ?: "Offer provider unavailable.")
                    else applyOffers(cached, error.message ?: "Showing cached offers.")
                }
        }
    }

    private fun load() {
        viewModelScope.launch {
            val cached = repository.loadCached()
            if (cached.isNotEmpty()) applyOffers(cached, null)
            refresh()
        }
    }

    private fun applyOffers(offers: List<Offer>, refreshError: String?) {
        _uiState.value = if (offers.isEmpty()) OffersUiState.Empty(refreshError) else OffersUiState.Content(offers, refreshError = refreshError)
    }

    private fun syncFailureMessage(result: ResourceSyncResult): String? = when (result) {
        ResourceSyncResult.NoProviders -> "No offer provider is configured."
        is ResourceSyncResult.Failed -> "Offer provider unavailable; no live refresh was applied."
        is ResourceSyncResult.Success -> if (result.failures.isEmpty()) null else "Some offer sources failed; showing the successful sources."
    }

    companion object {
        fun factory(repository: OfferCatalogRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = OffersViewModel(repository) as T
        }
    }
}
