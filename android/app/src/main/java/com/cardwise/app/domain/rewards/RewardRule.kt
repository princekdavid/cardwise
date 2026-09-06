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
        require(rewardRatePercent >= 0.0) { "Reward rate must not be negative" }
        require(maxRewardAmount == null || maxRewardAmount >= 0.0) { "Reward cap must not be negative" }
        require(minimumSpend >= 0.0) { "Minimum spend must not be negative" }
        require(maximumEligibleSpend == null || maximumEligibleSpend >= minimumSpend) {
            "Maximum eligible spend must be at least the minimum spend"
        }
    }
}
