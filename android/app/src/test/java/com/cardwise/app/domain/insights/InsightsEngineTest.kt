package com.cardwise.app.domain.insights

import com.cardwise.app.domain.model.PaymentHistoryEntry
import com.cardwise.app.domain.model.PaymentHistoryOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertFalse(summary.hasEnoughHistory)
        assertFalse(summary.milestones.first { it.id == "first_payment" }.completed)
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
        assertTrue(summary.hasEnoughHistory)
        assertTrue(summary.milestones.first { it.id == "first_payment" }.completed)
        assertEquals(3, summary.milestones.first { it.id == "ten_payments" }.progress)
        assertFalse(summary.milestones.first { it.id == "ten_payments" }.completed)
    }

    @Test
    fun tenPayments_completesPaymentMilestoneAndCapsProgress() {
        val entries = (1L..12L).map { entry(it, "Dining", 10.0) }
        val summary = engine.summarize(entries)
        val milestone = summary.milestones.first { it.id == "ten_payments" }
        assertTrue(milestone.completed)
        assertEquals(10, milestone.progress)
        assertEquals(120, summary.totalReward.toInt())
    }

    @Test
    fun hundredReward_completesRewardMilestoneAndCapsProgress() {
        val entries = listOf(
            entry(1, "Dining", 60.0),
            entry(2, "Travel", 55.0)
        )
        val summary = engine.summarize(entries)
        val milestone = summary.milestones.first { it.id == "hundred_reward" }
        assertTrue(milestone.completed)
        assertEquals(100, milestone.progress)
    }

    @Test
    fun blankCategory_isGroupedAsOther() {
        val summary = engine.summarize(listOf(entry(1, "   ", 12.5)))
        assertEquals(listOf("OTHER"), summary.categoryRewardTotals.map { it.first })
        assertEquals("12.5", summary.categoryRewardTotals.single().second.toPlainString())
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
