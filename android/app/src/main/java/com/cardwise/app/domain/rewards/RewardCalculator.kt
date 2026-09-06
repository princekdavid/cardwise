package com.cardwise.app.domain.rewards

import kotlin.math.min

data class RewardEstimate(
    val eligibleSpend: Double,
    val estimatedReward: Double,
    val capped: Boolean
)

/** Pure, deterministic reward estimation. It never mutates card or rule state. */
object RewardCalculator {
    fun estimate(spend: Double, rule: RewardRule): RewardEstimate {
        require(spend.isFinite() && spend >= 0.0) {
            "Spend must be a finite, non-negative value"
        }

        if (!rule.enabled || spend < rule.minimumSpend) {
            return RewardEstimate(
                eligibleSpend = 0.0,
                estimatedReward = 0.0,
                capped = false
            )
        }

        val eligibleSpend = rule.maximumEligibleSpend?.let { min(spend, it) } ?: spend
        val uncappedReward = eligibleSpend * rule.rewardRatePercent / 100.0
        val cappedReward = rule.maxRewardAmount?.let { min(uncappedReward, it) } ?: uncappedReward

        return RewardEstimate(
            eligibleSpend = eligibleSpend,
            estimatedReward = cappedReward,
            capped = cappedReward < uncappedReward
        )
    }
}
