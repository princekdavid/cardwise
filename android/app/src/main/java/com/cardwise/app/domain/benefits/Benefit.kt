package com.cardwise.app.domain.benefits

import com.cardwise.app.domain.resource.ResourceMetadata

/** Structured card benefit/rule supplied by a provider. */
data class Benefit(
    val benefitId: String,
    val productId: String,
    val category: String? = null,
    val merchantId: String? = null,
    val reward: BenefitReward,
    val minimumSpend: Double? = null,
    val maximumEligibleSpend: Double? = null,
    val maximumBenefit: Double? = null,
    val currency: String = "INR",
    val terms: String? = null,
    val metadata: ResourceMetadata
)

sealed interface BenefitReward {
    data class Percentage(val percent: Double) : BenefitReward
    data class FlatAmount(val amount: Double) : BenefitReward
    data class Points(val pointsPerCurrency: Double, val currencyValue: Double) : BenefitReward
}
