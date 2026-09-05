package com.cardwise.app.data.local

import androidx.room.Embedded
import androidx.room.Relation

 data class CardWithBenefits(
    @Embedded val card: CardEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "cardId"
    )
    val benefits: List<CardBenefitEntity>
)
