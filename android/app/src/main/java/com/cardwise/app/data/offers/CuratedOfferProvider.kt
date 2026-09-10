package com.cardwise.app.data.offers

import com.cardwise.app.domain.offers.Offer
import com.cardwise.app.domain.offers.OfferBenefit
import com.cardwise.app.domain.offers.OfferProvider
import com.cardwise.app.domain.resource.ResourceBatch
import com.cardwise.app.domain.resource.ResourceConfidence
import com.cardwise.app.domain.resource.ResourceMetadata
import com.cardwise.app.domain.resource.ResourceSourceType
import java.time.Instant

/** Bundled UX-safe offer seed. It is intentionally marked unverified until a live source is integrated. */
class CuratedOfferProvider : OfferProvider {
    override val providerId: String = "cardwise-curated-offers"

    override suspend fun fetch(): ResourceBatch<Offer> {
        val metadata = ResourceMetadata(
            sourceId = providerId,
            sourceType = ResourceSourceType.UNKNOWN,
            version = "2026-09",
            confidence = ResourceConfidence.UNKNOWN,
            effectiveFrom = Instant.parse("2026-09-01T00:00:00Z"),
            expiresAt = Instant.parse("2026-12-31T23:59:59Z")
        )
        val offers = listOf(
            Offer(
                offerId = "swiggy-dining-10cb-150",
                merchantId = "swiggy",
                merchantName = "Swiggy",
                title = "10% cashback on eligible spends",
                description = "Up to ₹150 • minimum spend ₹1,000",
                category = "Dining",
                benefit = OfferBenefit.PercentageCashback(10.0),
                minimumSpend = 1000.0,
                maximumBenefit = 150.0,
                terms = "Eligibility and terms must be verified before payment.",
                metadata = metadata
            ),
            Offer(
                offerId = "amazon-shopping-100cb",
                merchantId = "amazon",
                merchantName = "Amazon",
                title = "₹100 cashback on eligible purchases",
                description = "Minimum spend ₹2,000",
                category = "Shopping",
                benefit = OfferBenefit.FlatCashback(100.0),
                minimumSpend = 2000.0,
                maximumBenefit = 100.0,
                terms = "Eligibility and terms must be verified before payment.",
                metadata = metadata
            ),
            Offer(
                offerId = "travel-accelerated-rewards",
                merchantId = "*",
                merchantName = "Travel",
                title = "Accelerated travel rewards",
                description = "Selected travel transactions",
                category = "Travel",
                benefit = OfferBenefit.PercentageCashback(5.0),
                maximumBenefit = 500.0,
                terms = "Card-specific eligibility and caps must be verified before payment.",
                metadata = metadata
            )
        )
        return ResourceBatch(offers, metadata, System.currentTimeMillis())
    }
}
