package com.cardwise.app.domain.offers

import com.cardwise.app.domain.resource.ResourceMetadata

/** Structured merchant/card offer received from a resource provider. */
data class Offer(
    val offerId: String,
    val merchantId: String,
    val applicableProductIds: Set<String>,
    val benefit: OfferBenefit,
    val minimumSpend: Double? = null,
    val maximumBenefit: Double? = null,
    val currency: String = "INR",
    val stackable: Boolean = false,
    val terms: String? = null,
    val metadata: ResourceMetadata
)

sealed interface OfferBenefit {
    data class PercentageDiscount(val percent: Double) : OfferBenefit
    data class PercentageCashback(val percent: Double) : OfferBenefit
    data class FlatDiscount(val amount: Double) : OfferBenefit
    data class FlatCashback(val amount: Double) : OfferBenefit
}
