package com.cardwise.app.domain.offers

import com.cardwise.app.domain.resource.ResourceProvider
import com.cardwise.app.domain.resource.ResourceStore
import com.cardwise.app.domain.resource.ResourceSyncEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Owns the current provider-backed offer set and exposes refresh/cached reads to the UI. */
class OfferEngine(
    providers: List<ResourceProvider<Offer>>,
    private val store: ResourceStore<Offer>
) {
    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: Flow<List<Offer>> = _offers.asStateFlow()
    private val syncEngine = ResourceSyncEngine(providers, store)

    suspend fun loadCached() {
        _offers.value = store.read()
    }

    suspend fun refresh() {
        syncEngine.refresh()
        _offers.value = store.read()
    }
}
