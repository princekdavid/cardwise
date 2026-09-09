package com.cardwise.app.domain.catalog

import com.cardwise.app.domain.resource.ResourceProvider
import com.cardwise.app.domain.resource.ResourceStore
import com.cardwise.app.domain.resource.ResourceSyncEngine
import com.cardwise.app.domain.resource.ResourceSyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    /** Refreshes the catalogue and returns the provider outcome to the repository boundary. */
    suspend fun refresh(): ResourceSyncResult {
        val result = syncEngine.refresh()
        _products.value = store.read()
        return result
    }
}
