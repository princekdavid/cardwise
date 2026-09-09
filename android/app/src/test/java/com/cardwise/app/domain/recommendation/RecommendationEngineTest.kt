package com.cardwise.app.domain.recommendation

import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.rewards.RewardRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationEngineTest {
    private fun card(id: Long, active: Boolean = true) = Card(id, "Issuer$id", "Card $id", "000$id", CardNetwork.VISA, isActive = active)

    @Test fun ranksHighestRewardFirst() {
        val result = RecommendationEngine.recommend(PaymentContext("Dining", 10_000.0), listOf(card(1), card(2)), mapOf(1L to listOf(RewardRule("Dining", 2.0)), 2L to listOf(RewardRule("Dining", 5.0))))
        assertEquals(2L, result.first().card.id); assertEquals(500.0, result.first().reward.estimatedReward, 0.001); assertEquals(1, result.first().rank)
    }
    @Test fun ignoresInactiveCards() {
        val result = RecommendationEngine.recommend(PaymentContext("Dining", 1_000.0), listOf(card(1, false), card(2)), mapOf(1L to listOf(RewardRule("Dining", 10.0)), 2L to listOf(RewardRule("Dining", 2.0))))
        assertEquals(listOf(2L), result.map { it.card.id })
    }
    @Test fun ignoresWrongCategory() {
        assertEquals(0, RecommendationEngine.recommend(PaymentContext("Travel", 1_000.0), listOf(card(1)), mapOf(1L to listOf(RewardRule("Dining", 10.0)))).size)
    }
    @Test fun appliesRewardCap() {
        val result = RecommendationEngine.recommend(PaymentContext("Dining", 10_000.0), listOf(card(1)), mapOf(1L to listOf(RewardRule("Dining", 5.0, maxRewardAmount = 200.0))))
        assertEquals(200.0, result.single().reward.estimatedReward, 0.001); assertTrue(result.single().reward.capped)
    }
    @Test fun isDeterministicForTies() {
        val result = RecommendationEngine.recommend(PaymentContext("Dining", 1_000.0), listOf(card(2), card(1)), mapOf(1L to listOf(RewardRule("Dining", 2.0)), 2L to listOf(RewardRule("Dining", 2.0))))
        assertEquals(listOf(1L, 2L), result.map { it.card.id })
    }
    @Test fun ignoresZeroRewardAndZeroAmountRecommendations() { assertTrue(RecommendationEngine.recommend(PaymentContext("Dining", 0.0), listOf(card(1)), mapOf(1L to listOf(RewardRule("Dining", 10.0)))).isEmpty()) }
    @Test fun respectsMinimumAndMaximumEligibleSpend() {
        val result = RecommendationEngine.recommend(PaymentContext("Dining", 2_000.0), listOf(card(1)), mapOf(1L to listOf(RewardRule("Dining", 10.0, minimumSpend = 1_000.0, maximumEligibleSpend = 1_500.0))))
        assertEquals(1_500.0, result.single().reward.eligibleSpend, 0.001); assertEquals(150.0, result.single().reward.estimatedReward, 0.001)
    }
    @Test fun choosesBestRuleWhenMultipleRulesMatchCategory() {
        val result = RecommendationEngine.recommend(PaymentContext(" Dining ", 1_000.0), listOf(card(1)), mapOf(1L to listOf(RewardRule("Dining", 1.0), RewardRule(" dining ", 5.0), RewardRule("Travel", 20.0))))
        assertEquals(50.0, result.single().reward.estimatedReward, 0.001); assertTrue(result.single().reason.contains("5.00% rewards"))
    }
    @Test fun equivalentRewardRulesHaveStableExplanationRegardlessOfOrder() {
        val first = RecommendationEngine.recommend(PaymentContext("Dining", 1_000.0), listOf(card(1)), mapOf(1L to listOf(RewardRule("Dining", 2.0), RewardRule("Dining", 4.0, maxRewardAmount = 40.0)))).single()
        val second = RecommendationEngine.recommend(PaymentContext("Dining", 1_000.0), listOf(card(1)), mapOf(1L to listOf(RewardRule("Dining", 4.0, maxRewardAmount = 40.0), RewardRule("Dining", 2.0)))).single()
        assertEquals(first.reward, second.reward); assertEquals(first.reason, second.reason)
    }
    @Test fun ignoresDuplicateCardIdsDeterministically() {
        val result = RecommendationEngine.recommend(PaymentContext("Dining", 1_000.0), listOf(card(1), card(1)), mapOf(1L to listOf(RewardRule("Dining", 5.0))))
        assertEquals(listOf(1L), result.map { it.card.id })
    }
    @Test fun evaluationTraceExplainsMatchedDecisionDeterministically() {
        val evaluation = RecommendationEngine.evaluate(PaymentContext("Dining", 1_000.0), listOf(card(2), card(1)), mapOf(1L to listOf(RewardRule("Dining", 2.0)), 2L to listOf(RewardRule("Dining", 5.0))))
        assertEquals(RecommendationTraceOutcome.MATCHED, evaluation.trace.outcome)
        assertEquals(listOf("context", "eligibility", "benefit", "ranking"), evaluation.trace.steps.map { it.id })
        assertTrue(evaluation.trace.steps.single { it.id == "eligibility" }.detail.contains("2 active cards"))
        assertEquals(listOf(2L, 1L), evaluation.recommendations.map { it.card.id })
    }
    @Test fun evaluationTraceIsNoMatchWhenNoRuleApplies() {
        val trace = RecommendationEngine.evaluate(PaymentContext("Travel", 1_000.0), listOf(card(1)), mapOf(1L to listOf(RewardRule("Dining", 10.0)))).trace
        assertEquals(RecommendationTraceOutcome.NO_MATCH, trace.outcome); assertTrue(trace.steps.last().detail.contains("no eligible recommendation"))
    }
    @Test fun zeroAmountEvaluationLeavesDownstreamStepsPending() {
        val trace = RecommendationEngine.evaluate(PaymentContext("Dining", 0.0), emptyList(), emptyMap()).trace
        assertEquals(listOf(RecommendationTraceStatus.COMPLETED, RecommendationTraceStatus.COMPLETED, RecommendationTraceStatus.PENDING, RecommendationTraceStatus.PENDING), trace.steps.map { it.status })
    }
    @Test fun matchedRecommendationsExposeMathProvenanceAndWhyNotReason() {
        val result = RecommendationEngine.evaluate(
            PaymentContext("Dining", 2_000.0),
            listOf(card(1), card(2)),
            mapOf(
                1L to listOf(RewardRule("Dining", 5.0)),
                2L to listOf(RewardRule("Dining", 3.0, maximumEligibleSpend = 1_000.0))
            )
        ).recommendations
        assertTrue(result.first().provenance.contains("₹2,000.00 eligible spend × 5.00%"))
        assertEquals("", result.first().whyNot)
        assertTrue(result[1].whyNot.contains("₹70.00"))
        assertTrue(result[1].whyNot.contains("Only ₹1,000.00"))
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsNegativePaymentAmount() { PaymentContext("Dining", -1.0) }
    @Test(expected = IllegalArgumentException::class) fun rejectsInfinitePaymentAmount() { PaymentContext("Dining", Double.POSITIVE_INFINITY) }
    @Test(expected = IllegalArgumentException::class) fun rejectsNaNPaymentAmount() { PaymentContext("Dining", Double.NaN) }
    @Test(expected = IllegalArgumentException::class) fun rejectsBlankCategory() { PaymentContext("   ", 1_000.0) }
    @Test fun categoryMatchingIsCaseAndWhitespaceInsensitive() {
        val result = RecommendationEngine.recommend(PaymentContext("  dInInG  ", 1_000.0), listOf(card(1)), mapOf(1L to listOf(RewardRule(" DINING ", 5.0))))
        assertEquals(50.0, result.single().reward.estimatedReward, 0.001)
    }
}
