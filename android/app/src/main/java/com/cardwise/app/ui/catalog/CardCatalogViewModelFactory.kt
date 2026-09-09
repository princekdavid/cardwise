package com.cardwise.app.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cardwise.app.domain.catalog.CardCatalogRepository

class CardCatalogViewModelFactory(
    private val repository: CardCatalogRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CardCatalogViewModel(repository) as T
}
