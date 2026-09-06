package com.cardwise.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reward_rules")
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
