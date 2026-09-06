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
}
