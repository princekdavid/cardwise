package com.cardwise.app.data.local

import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "benefit_catalog", primaryKeys = ["cardId", "benefitId"], indices = [Index("cardId")])
data class BenefitCatalogEntity(
    val cardId: Long,
    val benefitId: String,
    val title: String,
    val description: String,
    val categories: String,
    val merchantHints: String,
    val rewardRatePercent: Double?,
    val maxRewardAmount: Double?,
    val minimumSpend: Double,
    val maximumEligibleSpend: Double?,
    val priority: Int,
    val catalogVersion: Long
)

@Entity(tableName = "benefit_catalog_metadata")
data class BenefitCatalogMetadataEntity(
    @androidx.room.PrimaryKey val id: Int = 1,
    val version: Long
)
