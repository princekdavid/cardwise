package com.cardwise.app.data.catalog

import com.cardwise.app.domain.catalog.CardProduct
import com.cardwise.app.domain.catalog.CardType
import com.cardwise.app.domain.catalog.MoneyAmount
import com.cardwise.app.domain.model.CardBenefit
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.resource.ResourceBatch
import com.cardwise.app.domain.resource.ResourceConfidence
import com.cardwise.app.domain.resource.ResourceMetadata
import com.cardwise.app.domain.resource.ResourceProvider
import com.cardwise.app.domain.resource.ResourceSourceType
import java.time.Instant

/**
 * Transitional built-in catalogue provider.
 *
 * This is intentionally marked UNKNOWN provenance: it is a deterministic local seed,
 * not a claim that current issuer terms have been verified. A production provider can
 * replace it without changing the catalog UI or domain contract.
 */
class CuratedCardCatalogProvider : ResourceProvider<CardProduct> {
    override val providerId: String = "cardwise-curated-seed"

    override suspend fun fetch(): ResourceBatch<CardProduct> {
        val now = Instant.now()
        val metadata = ResourceMetadata(
            sourceId = providerId,
            sourceType = ResourceSourceType.UNKNOWN,
            verifiedAt = null,
            version = "seed-v1",
            confidence = ResourceConfidence.UNKNOWN
        )
        return ResourceBatch(
            items = listOf(
                CardProduct("scapia-federal", "Scapia Federal Bank", "Scapia Travel Card", CardNetwork.RUPAY, CardType.CREDIT, MoneyAmount(0.0, "INR"), "Scapia rewards", listOf(CardBenefit("Travel rewards", "Travel-focused rewards and benefits", "travel")), metadata),
                CardProduct("amazon-pay-icici", "ICICI Bank", "Amazon Pay ICICI", CardNetwork.VISA, CardType.CREDIT, MoneyAmount(0.0, "INR"), "Amazon rewards", listOf(CardBenefit("Shopping rewards", "Higher value on eligible shopping spends", "shopping")), metadata),
                CardProduct("slice-super", "Slice", "Slice Super Card", CardNetwork.VISA, CardType.CREDIT, MoneyAmount(0.0, "INR"), "Slice rewards", listOf(CardBenefit("UPI benefits", "Benefits on eligible UPI spends", "upi")), metadata),
                CardProduct("hdfc-millennia", "HDFC Bank", "HDFC Millennia", CardNetwork.VISA, CardType.CREDIT, null, "Millennia rewards", listOf(CardBenefit("Online shopping", "Accelerated rewards on eligible online merchants", "shopping")), metadata),
                CardProduct("hdfc-swiggy", "HDFC Bank", "HDFC Swiggy", CardNetwork.MASTERCARD, CardType.CREDIT, null, "Swiggy rewards", listOf(CardBenefit("Food & grocery", "Accelerated rewards on eligible food and grocery spends", "dining")), metadata)
            ),
            metadata = metadata,
            fetchedAtEpochMillis = now.toEpochMilli()
        )
    }
}
