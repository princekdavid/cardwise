package com.cardwise.app.data.offers

import com.cardwise.app.domain.offers.Offer
import com.cardwise.app.domain.resource.ResourceBatch
import com.cardwise.app.domain.resource.ResourceMetadata
import com.cardwise.app.domain.resource.ResourceStore

class InMemoryOfferStore : ResourceStore<Offer> {
    private var items: List<Offer> = emptyList()
    private var metadata: ResourceMetadata? = null

    override suspend fun replace(batch: ResourceBatch<Offer>) {
        items = batch.items
        metadata = batch.metadata
    }

    override suspend fun read(): List<Offer> = items
}
