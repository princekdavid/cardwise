package com.cardwise.app.domain.offers

import com.cardwise.app.domain.resource.ResourceMetadata

/** Structured merchant/card promotion received from a resource provider. */
data class Offer(
    val offerId: String,
    val merchantId: String,
    val merchantName: String,
    val title: String,
    val description: String,
    val category: String? = null,
    val applicableProductIds: Set<String> = emptySet(),
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
