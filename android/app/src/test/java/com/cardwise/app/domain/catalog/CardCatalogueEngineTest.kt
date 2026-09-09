package com.cardwise.app.domain.catalog

import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.resource.ResourceBatch
import com.cardwise.app.domain.resource.ResourceConfidence
import com.cardwise.app.domain.resource.ResourceMetadata
import com.cardwise.app.domain.resource.ResourceProvider
import com.cardwise.app.domain.resource.ResourceSourceType
import com.cardwise.app.domain.resource.ResourceStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CardCatalogueEngineTest {
    private val metadata = ResourceMetadata(
        sourceId = "test-provider",
        sourceType = ResourceSourceType.OFFICIAL_ISSUER,
        confidence = ResourceConfidence.VERIFIED
    )

    @Test
    fun refresh_mergesProviderProductsIntoLocalStore() = runTest {
        val product = CardProduct("test-card", "Test Bank", "Test Card", CardNetwork.VISA, CardType.CREDIT, metadata = metadata)
        val store = FakeStore<CardProduct>()
        val provider = FakeProvider(listOf(product))
        val engine = CardCatalogueEngine(listOf(provider), store)

        engine.refresh()

        assertEquals(listOf(product), engine.products.first())
        assertEquals(listOf(product), store.read())
    }

    @Test
    fun refresh_withNoProviders_doesNotWriteCache() = runTest {
        val store = FakeStore<CardProduct>()
        val engine = CardCatalogueEngine(emptyList(), store)

        engine.refresh()

        assertTrue(store.read().isEmpty())
    }

    private class FakeProvider(private val products: List<CardProduct>) : ResourceProvider<CardProduct> {
        override val providerId = "test-provider"
        override suspend fun fetch() = ResourceBatch(products, metadata, 1L)
    }

    private class FakeStore<T> : ResourceStore<T> {
        private var items: List<T> = emptyList()
        override suspend fun replace(batch: ResourceBatch<T>) { items = batch.items }
        override suspend fun read(): List<T> = items
    }
}
