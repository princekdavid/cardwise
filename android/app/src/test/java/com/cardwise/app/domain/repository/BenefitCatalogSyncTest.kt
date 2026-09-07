package com.cardwise.app.domain.repository

import com.cardwise.app.domain.model.BenefitCatalogEntry
import com.cardwise.app.domain.model.BenefitCatalogSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BenefitCatalogSyncTest {
    @Test
    fun refresh_fetchesUsingCurrentVersionAndPersistsNewerSnapshot() = runTest {
        val initial = BenefitCatalogSnapshot(3L, listOf(entry("current")))
        val newer = BenefitCatalogSnapshot(4L, listOf(entry("newer")))
        val repository = FakeRepository(initial)
        val dataSource = RecordingDataSource(newer)

        val result = BenefitCatalogSync(repository, dataSource).refresh()

        assertEquals(3L, dataSource.requestedVersion)
        assertEquals(BenefitCatalogSync.Result.Updated(4L), result)
        assertEquals(newer, repository.current.value)
    }

    @Test
    fun refresh_returnsNoUpdateWhenSourceHasNothingNew() = runTest {
        val repository = FakeRepository(BenefitCatalogSnapshot(3L, emptyList()))
        val dataSource = RecordingDataSource(null)

        val result = BenefitCatalogSync(repository, dataSource).refresh()

        assertEquals(BenefitCatalogSync.Result.NoUpdate, result)
        assertEquals(3L, dataSource.requestedVersion)
        assertEquals(0, repository.replaceCount)
    }

    @Test
    fun refresh_doesNotReportUpdateForStaleSnapshot() = runTest {
        val repository = FakeRepository(BenefitCatalogSnapshot(5L, listOf(entry("current"))))
        val dataSource = RecordingDataSource(BenefitCatalogSnapshot(4L, listOf(entry("stale"))))

        val result = BenefitCatalogSync(repository, dataSource).refresh()

        assertEquals(BenefitCatalogSync.Result.NoUpdate, result)
        assertEquals(5L, dataSource.requestedVersion)
        assertTrue(repository.current.value.entries.single().benefitId == "stale")
    }

    private fun entry(id: String) = BenefitCatalogEntry(
        cardId = 1L,
        benefitId = id,
        title = id,
        description = "Benefit $id"
    )

    private class FakeRepository(initial: BenefitCatalogSnapshot) : BenefitCatalogRepository {
        val current = MutableStateFlow(initial)
        var replaceCount = 0
            private set

        override fun observeCatalog(): Flow<BenefitCatalogSnapshot> = current.asStateFlow()

        override suspend fun replaceCatalog(snapshot: BenefitCatalogSnapshot) {
            replaceCount++
            current.value = snapshot
        }
    }

    private class RecordingDataSource(private val response: BenefitCatalogSnapshot?) : BenefitCatalogDataSource {
        var requestedVersion: Long? = null
            private set

        override suspend fun fetchCatalog(currentVersion: Long): BenefitCatalogSnapshot? {
            requestedVersion = currentVersion
            return response
        }
    }
}
