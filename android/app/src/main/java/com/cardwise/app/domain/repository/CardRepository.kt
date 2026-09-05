package com.cardwise.app.domain.repository

import com.cardwise.app.domain.model.Card
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    fun observeCards(): Flow<List<Card>>
    suspend fun addCard(card: Card)
    suspend fun updateCard(card: Card)
    suspend fun deleteCard(cardId: Long)
}
