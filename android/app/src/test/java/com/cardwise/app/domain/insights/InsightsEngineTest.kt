package com.cardwise.app.domain.insights

import com.cardwise.app.domain.model.PaymentHistoryEntry
import com.cardwise.app.domain.model.PaymentHistoryOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightsEngineTest {
    private val engine = InsightsEngine()

    @Test
    fun emptyHistory_returnsEmptySummary() {
        val summary = engine.summarize(emptyList())
        assertEquals(0, summary.paymentCount)
        assertEquals("0", summary.totalReward.toPlainString())
        assertTrue(summary.categoryRewardTotals.isEmpty())
    }

    @Test
    fun summary_aggregatesRewardsAndCategoriesDeterministically() {
        val entries = listOf(
            entry(1, "Dining", 40.0),
            entry(2, "travel", 25.0),
            entry(3, "DINING", 10.0)
        )
        val summary = engine.summarize(entries)
        assertEquals(3, summary.paymentCount)
        assertEquals("75.0", summary.totalReward.toPlainString())
        assertEquals(listOf("DINING", "TRAVEL"), summary.categoryRewardTotals.map { it.first })
        assertEquals("50.0", summary.categoryRewardTotals.first().second.toPlainString())
        assertTrue(summary.milestones.first().completed)
    }

    private fun entry(id: Long, category: String, reward: Double) = PaymentHistoryEntry(
        id = id,
        occurredAtEpochMillis = id,
        amount = 1000.0,
        category = category,
        cardId = 1L,
        rewardAmount = reward,
        outcome = PaymentHistoryOutcome.HANDOFF_STARTED
    )
}
