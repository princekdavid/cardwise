package com.cardwise.app.data.repository

import androidx.room.withTransaction
import com.cardwise.app.data.local.CardBenefitEntity
import com.cardwise.app.data.local.CardDatabase
import com.cardwise.app.data.local.CardEntity
import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardBenefit
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCardRepository(
    private val database: CardDatabase
) : CardRepository {
    private val dao = database.cardDao()

    override fun observeCards(): Flow<List<Card>> =
        dao.observeCards().map { cards -> cards.map(::toDomain) }

    override suspend fun addCard(card: Card): Long = database.withTransaction {
        val id = dao.insertCard(card.toEntity(id = 0L))
        if (card.benefits.isNotEmpty()) {
            dao.insertBenefits(card.benefits.map { it.toEntity(id) })
        }
        id
    }

    override suspend fun updateCard(card: Card) = database.withTransaction {
        dao.updateCard(card.toEntity())
        dao.deleteBenefits(card.id)
        if (card.benefits.isNotEmpty()) {
            dao.insertBenefits(card.benefits.map { it.toEntity(card.id) })
        }
    }

    override suspend fun deleteCard(cardId: Long) = database.withTransaction {
        dao.deleteBenefits(cardId)
        dao.deleteCard(
            CardEntity(
                id = cardId,
                issuer = "",
                name = "",
                lastFour = "",
                network = CardNetwork.OTHER.name
            )
        )
    }

    private fun toDomain(item: com.cardwise.app.data.local.CardWithBenefits): Card =
        Card(
            id = item.card.id,
            issuer = item.card.issuer,
            name = item.card.name,
            lastFour = item.card.lastFour,
            network = runCatching { CardNetwork.valueOf(item.card.network) }
                .getOrDefault(CardNetwork.OTHER),
            isActive = item.card.isActive,
            benefits = item.benefits.map {
                CardBenefit(it.title, it.description, it.category)
            }
        )

    private fun Card.toEntity(id: Long = this.id) = CardEntity(
        id = id,
        issuer = issuer,
        name = name,
        lastFour = lastFour,
        network = network.name,
        isActive = isActive
    )

    private fun CardBenefit.toEntity(cardId: Long) = CardBenefitEntity(
        cardId = cardId,
        title = title,
        description = description,
        category = category
    )
}
