package com.cardwise.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_history")
data class PaymentHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val occurredAtEpochMillis: Long,
    val amount: Double,
    val category: String,
    val cardId: Long,
    val rewardAmount: Double,
    val outcome: String
)
