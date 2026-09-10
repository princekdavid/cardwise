package com.cardwise.app.data.repository

import com.cardwise.app.data.local.CardDatabase
import com.cardwise.app.data.local.PaymentHistoryEntity
import com.cardwise.app.domain.model.PaymentHistoryEntry
import com.cardwise.app.domain.model.PaymentHistoryOutcome
import com.cardwise.app.domain.repository.PaymentHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPaymentHistoryRepository(
    database: CardDatabase
) : PaymentHistoryRepository {
    private val dao = database.paymentHistoryDao()

    override fun observeEntries(): Flow<List<PaymentHistoryEntry>> =
        dao.observeEntries().map { entries -> entries.map(::toDomain) }

    override suspend fun record(entry: PaymentHistoryEntry) {
        dao.insert(
            PaymentHistoryEntity(
                id = entry.id,
                occurredAtEpochMillis = entry.occurredAtEpochMillis,
                amount = entry.amount,
                category = entry.category,
                cardId = entry.cardId,
                rewardAmount = entry.rewardAmount,
                outcome = entry.outcome.name
            )
        )
    }

    override suspend fun clear() = dao.clear()

    private fun toDomain(entry: PaymentHistoryEntity) = PaymentHistoryEntry(
        id = entry.id,
        occurredAtEpochMillis = entry.occurredAtEpochMillis,
        amount = entry.amount,
        category = entry.category,
        cardId = entry.cardId,
        rewardAmount = entry.rewardAmount,
        outcome = runCatching { PaymentHistoryOutcome.valueOf(entry.outcome) }
            .getOrDefault(PaymentHistoryOutcome.HANDOFF_STARTED)
    )
}
