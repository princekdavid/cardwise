package com.cardwise.app.domain.benefits

import com.cardwise.app.domain.resource.ResourceProvider
import com.cardwise.app.domain.resource.ResourceStore
import com.cardwise.app.domain.resource.ResourceSyncEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Owns the current provider-backed benefit/rule set and exposes refresh/cached reads. */
class BenefitEngine(
    providers: List<ResourceProvider<Benefit>>,
    private val store: ResourceStore<Benefit>
) {
    private val _benefits = MutableStateFlow<List<Benefit>>(emptyList())
    val benefits: Flow<List<Benefit>> = _benefits.asStateFlow()
    private val syncEngine = ResourceSyncEngine(providers, store)

    suspend fun loadCached() {
        _benefits.value = store.read()
    }

    suspend fun refresh() {
        syncEngine.refresh()
        _benefits.value = store.read()
    }
}
