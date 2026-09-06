package com.cardwise.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cardwise.app.data.local.CardDatabase
import com.cardwise.app.data.repository.RoomRewardRuleRepository
import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.repository.CardRepository
import com.cardwise.app.data.repository.RoomCardRepository
import com.cardwise.app.domain.rewards.RewardRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomRewardRuleRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val database = Room.inMemoryDatabaseBuilder(context, CardDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    private val cardRepository: CardRepository = RoomCardRepository(database)
    private val rewardRuleRepository = RoomRewardRuleRepository(database)

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun replaceRules_persistsAndReplacesRules() = runBlocking {
        val cardId = cardRepository.addCard(
            Card(0L, "HDFC", "Dining Card", "4321", CardNetwork.VISA)
        )

        rewardRuleRepository.replaceRules(
            cardId,
            listOf(RewardRule("Dining", 5.0), RewardRule("Travel", 2.0))
        )
        assertEquals(
            listOf("Dining", "Travel"),
            rewardRuleRepository.getRules(cardId).map { it.category }
        )

        rewardRuleRepository.replaceRules(cardId, listOf(RewardRule("Dining", 7.5)))

        assertEquals(
            listOf(RewardRule("Dining", 7.5)),
            rewardRuleRepository.getRules(cardId)
        )
        assertEquals(
            mapOf(cardId to listOf(RewardRule("Dining", 7.5))),
            rewardRuleRepository.observeRules().first()
        )
    }

    @Test
    fun deletingCard_cascadesToRewardRules() = runBlocking {
        val cardId = cardRepository.addCard(
            Card(0L, "ICICI", "Travel Card", "1234", CardNetwork.MASTERCARD)
        )
        rewardRuleRepository.replaceRules(cardId, listOf(RewardRule("Travel", 3.0)))

        cardRepository.deleteCard(cardId)

        assertEquals(emptyList<RewardRule>(), rewardRuleRepository.getRules(cardId))
        assertEquals(emptyMap<Long, List<RewardRule>>(), rewardRuleRepository.observeRules().first())
    }
}
