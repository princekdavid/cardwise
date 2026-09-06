package com.cardwise.app.domain.rewards

/**
 * User-configured reward rule for a card/category combination.
 * All amounts are expressed in the user's currency unit.
 */
data class RewardRule(
    val category: String,
    val rewardRatePercent: Double,
    val maxRewardAmount: Double? = null,
    val minimumSpend: Double = 0.0,
    val maximumEligibleSpend: Double? = null,
    val enabled: Boolean = true
) {
    init {
        require(category.isNotBlank()) { "Category must not be blank" }
        require(rewardRatePercent.isFinite() && rewardRatePercent >= 0.0) {
            "Reward rate must be a finite, non-negative value"
        }
        require(maxRewardAmount == null || (maxRewardAmount.isFinite() && maxRewardAmount >= 0.0)) {
            "Reward cap must be a finite, non-negative value"
        }
        require(minimumSpend.isFinite() && minimumSpend >= 0.0) {
            "Minimum spend must be a finite, non-negative value"
        }
        require(maximumEligibleSpend == null ||
            (maximumEligibleSpend.isFinite() && maximumEligibleSpend >= minimumSpend)
        ) {
            "Maximum eligible spend must be finite and at least the minimum spend"
        }
    }
}
