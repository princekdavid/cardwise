package com.cardwise.app.domain.benefits

/**
 * Tracks usage of a card benefit during a defined period.
 * No payment credentials or transaction details are stored here.
 */
data class BenefitTracking(
    val cardId: Long,
    val benefitId: Long,
    val usedAmount: Double = 0.0,
    val allowance: Double? = null,
    val periodStartEpochDay: Long,
    val periodEndEpochDay: Long? = null,
    val expiresAtEpochDay: Long? = null
) {
    init {
        require(cardId > 0) { "Card id must be positive" }
        require(benefitId > 0) { "Benefit id must be positive" }
        require(usedAmount >= 0.0) { "Used amount must not be negative" }
        require(allowance == null || allowance >= 0.0) { "Allowance must not be negative" }
        require(periodEndEpochDay == null || periodEndEpochDay >= periodStartEpochDay) {
            "Period end must not precede period start"
        }
    }
}

object BenefitTracker {
    fun remaining(tracking: BenefitTracking): Double? =
        tracking.allowance?.let { (it - tracking.usedAmount).coerceAtLeast(0.0) }

    fun progress(tracking: BenefitTracking): Double? =
        tracking.allowance?.takeIf { it > 0.0 }?.let {
            (tracking.usedAmount / it).coerceIn(0.0, 1.0)
        }

    fun recordUsage(tracking: BenefitTracking, amount: Double): BenefitTracking {
        require(amount >= 0.0) { "Usage amount must not be negative" }
        return tracking.copy(usedAmount = tracking.usedAmount + amount)
    }
}
