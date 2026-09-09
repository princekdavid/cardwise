package com.cardwise.app.data.catalog

import com.cardwise.app.domain.catalog.CardProduct
import com.cardwise.app.domain.resource.ResourceBatch
import com.cardwise.app.domain.resource.ResourceStore

/**
 * Process-local cache used by the current built-in provider.
 * Replace with a Room-backed catalog store when a remote provider is introduced.
 */
class InMemoryCardCatalogStore : ResourceStore<CardProduct> {
    private var cached: List<CardProduct> = emptyList()

    override suspend fun replace(batch: ResourceBatch<CardProduct>) {
        cached = batch.items
    }

    override suspend fun read(): List<CardProduct> = cached
}
