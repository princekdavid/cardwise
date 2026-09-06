package com.cardwise.app.domain.model

/** Safe-to-store representation of a payment card. Never store PAN, CVV, PIN, or full track data. */
data class Card(
    val id: Long,
    val issuer: String,
    val name: String,
    val lastFour: String,
    val network: CardNetwork,
    val isActive: Boolean = true,
    val benefits: List<CardBenefit> = emptyList()
)

enum class CardNetwork { VISA, MASTERCARD, AMEX, RUPAY, OTHER }

data class CardBenefit(
    val title: String,
    val description: String,
    val category: String? = null
)
