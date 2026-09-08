package com.cardwise.app.domain.resource

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResourceSyncEngineTest {
    @Test
    fun refresh_mergesSuccessfulProvidersAndPreservesPartialFailures() = runTest {
        val store = InMemoryStore()
        val providerA = FakeProvider("issuer-a", listOf("card-a", "shared"))
        val providerB = FakeProvider("issuer-b", listOf("card-b", "shared"))
        val failing = FailingProvider("broken")

        val result = ResourceSyncEngine(
            providers = listOf(providerA, providerB, failing),
            store = store
        ).refresh()

        assertTrue(result is ResourceSyncResult.Success)
        result as ResourceSyncResult.Success
        assertEquals(2, result.providerCount)
        assertEquals(1, result.failures.size)
        assertEquals(listOf("card-a", "shared", "card-b"), store.items)
    }

    @Test
    fun refresh_failsWithoutOverwritingCacheWhenEveryProviderFails() = runTest {
        val store = InMemoryStore().apply { items = listOf("cached") }
        val result = ResourceSyncEngine(
            providers = listOf(FailingProvider("broken")),
            store = store
        ).refresh()

        assertTrue(result is ResourceSyncResult.Failed)
        assertEquals(listOf("cached"), store.items)
    }

    private class FakeProvider(
        override val providerId: String,
        private val values: List<String>
    ) : ResourceProvider<String> {
        override suspend fun fetch() = ResourceBatch(
            items = values,
            metadata = ResourceMetadata(providerId, ResourceSourceType.OFFICIAL_ISSUER),
            fetchedAtEpochMillis = 1L
        )
    }

    private class FailingProvider(
        override val providerId: String
    ) : ResourceProvider<String> {
        override suspend fun fetch(): ResourceBatch<String> = error("provider unavailable")
    }

    private class InMemoryStore : ResourceStore<String> {
        var items: List<String> = emptyList()
        override suspend fun replace(batch: ResourceBatch<String>) { items = batch.items }
        override suspend fun read(): List<String> = items
    }
}
