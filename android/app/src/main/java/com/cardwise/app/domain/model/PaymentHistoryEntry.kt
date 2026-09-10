package com.cardwise.app.domain.model

data class PaymentHistoryEntry(
    val id: Long,
    val occurredAtEpochMillis: Long,
    val amount: Double,
    val category: String,
    val cardId: Long,
    val rewardAmount: Double,
    val outcome: PaymentHistoryOutcome
)

enum class PaymentHistoryOutcome { HANDOFF_STARTED }
