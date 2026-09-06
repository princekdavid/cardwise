package com.cardwise.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.cardwise.app.data.local.CardDatabase
import com.cardwise.app.data.repository.RoomBenefitCatalogRepository
import com.cardwise.app.domain.model.BenefitCatalogEntry
import com.cardwise.app.domain.model.BenefitCatalogSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RoomBenefitCatalogRepositoryTest {
    private lateinit var database: CardDatabase
    private lateinit var repository: RoomBenefitCatalogRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CardDatabase::class.java).build()
        repository = RoomBenefitCatalogRepository(database)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun replaceCatalog_persistsVersionAndEntries() = runBlocking {
        val entry = BenefitCatalogEntry(
            cardId = 1L,
            benefitId = "dining-5",
            title = "Dining rewards",
            description = "Earn 5 percent on eligible dining.",
            categories = setOf("dining"),
            merchantHints = setOf("restaurant"),
            rewardRatePercent = 5.0,
            priority = 10
        )

        repository.replaceCatalog(BenefitCatalogSnapshot(7L, listOf(entry)))

        val snapshot = repository.observeCatalog().first()
        assertEquals(7L, snapshot.version)
        assertEquals(listOf(entry), snapshot.entries)
    }

    @Test
    fun replaceCatalog_replacesPreviousSnapshotAtomically() = runBlocking {
        val first = BenefitCatalogEntry(1L, "old", "Old", "Old benefit")
        val second = BenefitCatalogEntry(1L, "new", "New", "New benefit")

        repository.replaceCatalog(BenefitCatalogSnapshot(1L, listOf(first)))
        repository.replaceCatalog(BenefitCatalogSnapshot(2L, listOf(second)))

        val snapshot = repository.observeCatalog().first()
        assertEquals(2L, snapshot.version)
        assertEquals(listOf(second), snapshot.entries)
    }

    @Test
    fun replaceCatalog_ignoresStaleSnapshot() = runBlocking {
        val current = BenefitCatalogEntry(1L, "current", "Current", "Current benefit")
        val stale = BenefitCatalogEntry(1L, "stale", "Stale", "Stale benefit")

        repository.replaceCatalog(BenefitCatalogSnapshot(5L, listOf(current)))
        repository.replaceCatalog(BenefitCatalogSnapshot(4L, listOf(stale)))

        val snapshot = repository.observeCatalog().first()
        assertEquals(5L, snapshot.version)
        assertEquals(listOf(current), snapshot.entries)
    }

    @Test
    fun replaceCatalog_ignoresEqualVersionSnapshot() = runBlocking {
        val current = BenefitCatalogEntry(1L, "current", "Current", "Current benefit")
        val replacement = BenefitCatalogEntry(1L, "replacement", "Replacement", "Replacement benefit")

        repository.replaceCatalog(BenefitCatalogSnapshot(5L, listOf(current)))
        repository.replaceCatalog(BenefitCatalogSnapshot(5L, listOf(replacement)))

        val snapshot = repository.observeCatalog().first()
        assertEquals(5L, snapshot.version)
        assertEquals(listOf(current), snapshot.entries)
    }

    @Test
    fun replaceCatalog_acceptsNewerSnapshot() = runBlocking {
        val current = BenefitCatalogEntry(1L, "current", "Current", "Current benefit")
        val newer = BenefitCatalogEntry(1L, "newer", "Newer", "Newer benefit")

        repository.replaceCatalog(BenefitCatalogSnapshot(5L, listOf(current)))
        repository.replaceCatalog(BenefitCatalogSnapshot(6L, listOf(newer)))

        val snapshot = repository.observeCatalog().first()
        assertEquals(6L, snapshot.version)
        assertEquals(listOf(newer), snapshot.entries)
    }
}
