package com.cardwise.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "card_benefits",
    foreignKeys = [
        ForeignKey(
            entity = CardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cardId"])]
)
data class CardBenefitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val cardId: Long,
    val title: String,
    val description: String,
    val category: String? = null
)
