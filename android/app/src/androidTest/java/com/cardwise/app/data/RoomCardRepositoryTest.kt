package com.cardwise.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cardwise.app.data.local.CardDatabase
import com.cardwise.app.data.repository.RoomCardRepository
import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardBenefit
import com.cardwise.app.domain.model.CardNetwork
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomCardRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val database = Room.inMemoryDatabaseBuilder(context, CardDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    private val repository = RoomCardRepository(database)

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun addCard_persistsCardAndBenefits() = runBlocking {
        val id = repository.addCard(
            Card(
                id = 0L,
                issuer = "HDFC",
                name = "Dining Card",
                lastFour = "4321",
                network = CardNetwork.VISA,
                benefits = listOf(CardBenefit("Dining offer", "5% back", "Dining"))
            )
        )

        val persisted = repository.observeCards().first()
        assertEquals(1, persisted.size)
        assertEquals(id, persisted.single().id)
        assertEquals("Dining Card", persisted.single().name)
        assertEquals(listOf(CardBenefit("Dining offer", "5% back", "Dining")), persisted.single().benefits)
    }

    @Test
    fun updateCard_replacesBenefitsAtomically() = runBlocking {
        val id = repository.addCard(
            Card(
                0L,
                "ICICI",
                "Rewards",
                "1234",
                CardNetwork.MASTERCARD,
                benefits = listOf(CardBenefit("Old", "Old benefit"))
            )
        )

        repository.updateCard(
            Card(
                id,
                "ICICI",
                "Rewards Plus",
                "1234",
                CardNetwork.MASTERCARD,
                isActive = false,
                benefits = listOf(CardBenefit("New", "New benefit", "Travel"))
            )
        )

        assertEquals(
            listOf(
                Card(
                    id,
                    "ICICI",
                    "Rewards Plus",
                    "1234",
                    CardNetwork.MASTERCARD,
                    isActive = false,
                    benefits = listOf(CardBenefit("New", "New benefit", "Travel"))
                )
            ),
            repository.observeCards().first()
        )
    }

    @Test
    fun deleteCard_removesCardAndBenefits() = runBlocking {
        val id = repository.addCard(
            Card(
                0L,
                "SBI",
                "Cashback",
                "9999",
                CardNetwork.RUPAY,
                benefits = listOf(CardBenefit("Offer", "Cashback"))
            )
        )

        repository.deleteCard(id)

        assertEquals(emptyList<Card>(), repository.observeCards().first())
    }
}
