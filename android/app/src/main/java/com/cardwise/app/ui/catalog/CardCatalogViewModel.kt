package com.cardwise.app.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardwise.app.domain.catalog.CardCatalogRefreshResult
import com.cardwise.app.domain.catalog.CardCatalogRepository
import com.cardwise.app.domain.catalog.CardProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CardCatalogViewModel(
    private val repository: CardCatalogRepository
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(CatalogFilter.ALL)
    private val loading = MutableStateFlow(true)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CardCatalogUiState> = combine(
        repository.products, query, filter, loading, error
    ) { products, q, selected, isLoading, failure ->
        val visible = products.filter { product ->
            val matchesQuery = q.isBlank() || product.name.contains(q, true) || product.issuer.contains(q, true)
            val category = product.benefits.firstOrNull()?.category.orEmpty()
            val matchesFilter = when (selected) {
                CatalogFilter.ALL -> true
                CatalogFilter.CREDIT -> product.cardType.name == "CREDIT"
                CatalogFilter.TRAVEL -> category == "travel"
                CatalogFilter.SHOPPING -> category == "shopping"
                CatalogFilter.UPI -> category == "upi"
            }
            matchesQuery && matchesFilter
        }
        when {
            isLoading && products.isEmpty() -> CardCatalogUiState.Loading
            failure != null && products.isEmpty() -> CardCatalogUiState.Unavailable(failure)
            visible.isEmpty() -> CardCatalogUiState.Empty(q, selected)
            else -> CardCatalogUiState.Content(products, visible, q, selected, failure)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CardCatalogUiState.Loading)

    init {
        viewModelScope.launch {
            repository.loadCached()
            loading.value = false
            refresh()
        }
    }

    fun setQuery(value: String) { query.value = value }

    fun setFilter(value: CatalogFilter) { filter.value = value }

    fun refresh() {
        viewModelScope.launch {
            loading.value = true
            error.value = null
            when (val result = repository.refresh()) {
                is CardCatalogRefreshResult.Success -> Unit
                is CardCatalogRefreshResult.Unavailable -> error.value = result.reason
            }
            loading.value = false
        }
    }
}

enum class CatalogFilter { ALL, CREDIT, TRAVEL, SHOPPING, UPI }

sealed interface CardCatalogUiState {
    data object Loading : CardCatalogUiState
    data class Content(
        val products: List<CardProduct>,
        val visibleProducts: List<CardProduct>,
        val query: String,
        val filter: CatalogFilter,
        val refreshError: String? = null
    ) : CardCatalogUiState
    data class Empty(val query: String, val filter: CatalogFilter) : CardCatalogUiState
    data class Unavailable(val message: String) : CardCatalogUiState
}
