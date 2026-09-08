package com.cardwise.app.domain.catalog

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.cardwise.app.domain.resource.ResourceProvider
import com.cardwise.app.domain.resource.ResourceStore
import com.cardwise.app.domain.resource.ResourceSyncEngine

/**
 * Owns the current card catalogue. The UI only consumes this engine; it never owns card data.
 */
class CardCatalogueEngine(
    providers: List<ResourceProvider<CardProduct>>,
    private val store: ResourceStore<CardProduct>
) {
    private val _products = MutableStateFlow<List<CardProduct>>(emptyList())
    val products: Flow<List<CardProduct>> = _products.asStateFlow()
    private val syncEngine = ResourceSyncEngine(providers, store)

    suspend fun loadCached() {
        _products.value = store.read()
    }

    suspend fun refresh() {
        syncEngine.refresh()
        _products.value = store.read()
    }
}
