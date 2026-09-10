package com.cardwise.app.domain.repository

import com.cardwise.app.domain.model.PaymentHistoryEntry
import kotlinx.coroutines.flow.Flow

interface PaymentHistoryRepository {
    fun observeEntries(): Flow<List<PaymentHistoryEntry>>
    suspend fun record(entry: PaymentHistoryEntry)
    suspend fun clear()
}
