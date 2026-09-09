package com.cardwise.app.domain.catalog

import com.cardwise.app.domain.model.CardBenefit
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.resource.ResourceMetadata

/** Dynamic catalogue definition. User-specific enrollment stays in Card/UserCard storage. */
data class CardProduct(
    val productId: String,
    val issuer: String,
    val name: String,
    val network: CardNetwork,
    val cardType: CardType,
    val annualFee: MoneyAmount? = null,
    val rewardProgram: String? = null,
    val benefits: List<CardBenefit> = emptyList(),
    val metadata: ResourceMetadata
)

enum class CardType {
    CREDIT,
    DEBIT,
    PREPAID,
    OTHER
}

data class MoneyAmount(
    val amount: Double,
    val currency: String
)
