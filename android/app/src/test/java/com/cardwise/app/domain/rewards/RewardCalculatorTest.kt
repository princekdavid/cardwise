package com.cardwise.app.domain.rewards

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardCalculatorTest {
    @Test
    fun calculatesPercentageReward() {
        val result = RewardCalculator.estimate(
            spend = 2_000.0,
            rule = RewardRule(category = "Dining", rewardRatePercent = 5.0)
        )

        assertEquals(2_000.0, result.eligibleSpend, 0.001)
        assertEquals(100.0, result.estimatedReward, 0.001)
        assertFalse(result.capped)
    }

    @Test
    fun appliesMaximumEligibleSpendBeforeCalculatingReward() {
        val result = RewardCalculator.estimate(
            spend = 5_000.0,
            rule = RewardRule(
                category = "Dining",
                rewardRatePercent = 10.0,
                maximumEligibleSpend = 2_000.0
            )
        )

        assertEquals(2_000.0, result.eligibleSpend, 0.001)
        assertEquals(200.0, result.estimatedReward, 0.001)
        assertFalse(result.capped)
    }

    @Test
    fun appliesRewardCap() {
        val result = RewardCalculator.estimate(
            spend = 5_000.0,
            rule = RewardRule(
                category = "Travel",
                rewardRatePercent = 5.0,
                maxRewardAmount = 150.0
            )
        )

        assertEquals(150.0, result.estimatedReward, 0.001)
        assertTrue(result.capped)
    }

    @Test
    fun minimumSpendMakesRuleIneligible() {
        val result = RewardCalculator.estimate(
            spend = 499.0,
            rule = RewardRule(
                category = "Shopping",
                rewardRatePercent = 5.0,
                minimumSpend = 500.0
            )
        )

        assertEquals(0.0, result.estimatedReward, 0.001)
        assertEquals(0.0, result.eligibleSpend, 0.001)
    }

    @Test
    fun disabledRuleProducesNoReward() {
        val result = RewardCalculator.estimate(
            spend = 1_000.0,
            rule = RewardRule(
                category = "Fuel",
                rewardRatePercent = 4.0,
                enabled = false
            )
        )

        assertEquals(0.0, result.estimatedReward, 0.001)
    }
}
