package com.cardwise.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reward_rules",
    foreignKeys = [
        ForeignKey(
            entity = CardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cardId")]
)
data class RewardRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val cardId: Long,
    val category: String,
    val rewardRatePercent: Double,
    val maxRewardAmount: Double?,
    val minimumSpend: Double,
    val maximumEligibleSpend: Double?,
    val enabled: Boolean
)
