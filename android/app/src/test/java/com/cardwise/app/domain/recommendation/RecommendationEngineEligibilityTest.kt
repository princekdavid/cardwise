package com.cardwise.app.domain.recommendation

import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.rewards.RewardRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationEngineEligibilityTest {
    private fun card(id: Long, active: Boolean = true) = Card(
        id = id,
        issuer = "Issuer$id",
        name = "Card $id",
        lastFour = "000$id",
        network = CardNetwork.VISA,
        isActive = active
    )

    @Test fun disabledHighRewardRuleDoesNotDisplaceEligibleCard() {
        val result = RecommendationEngine.recommend(
            PaymentContext("Dining", 1_000.0),
            listOf(card(1), card(2)),
            mapOf(
                1L to listOf(RewardRule("Dining", 10.0, enabled = false)),
                2L to listOf(RewardRule("Dining", 2.0))
            )
        )

        assertEquals(listOf(2L), result.map { it.card.id })
    }

    @Test fun ruleBelowMinimumSpendDoesNotProduceRecommendation() {
        val result = RecommendationEngine.recommend(
            PaymentContext("Dining", 999.0),
            listOf(card(1)),
            mapOf(1L to listOf(RewardRule("Dining", 10.0, minimumSpend = 1_000.0)))
        )

        assertTrue(result.isEmpty())
    }

    @Test fun zeroRateRuleDoesNotProduceRecommendation() {
        val result = RecommendationEngine.recommend(
            PaymentContext("Dining", 1_000.0),
            listOf(card(1)),
            mapOf(1L to listOf(RewardRule("Dining", 0.0)))
        )

        assertTrue(result.isEmpty())
    }

    @Test fun activeDuplicateIsNotLostWhenEarlierDuplicateIsInactive() {
        val result = RecommendationEngine.recommend(
            PaymentContext("Dining", 1_000.0),
            listOf(card(1, active = false), card(1, active = true)),
            mapOf(1L to listOf(RewardRule("Dining", 5.0)))
        )

        assertEquals(listOf(1L), result.map { it.card.id })
        assertEquals(1, result.single().rank)
    }

    @Test fun recommendationReasonIncludesCapWhenRewardIsCapped() {
        val result = RecommendationEngine.recommend(
            PaymentContext("Dining", 10_000.0),
            listOf(card(1)),
            mapOf(1L to listOf(RewardRule("Dining", 5.0, maxRewardAmount = 100.0)))
        )

        assertTrue(result.single().reason.contains("(cap applied)"))
    }
}
