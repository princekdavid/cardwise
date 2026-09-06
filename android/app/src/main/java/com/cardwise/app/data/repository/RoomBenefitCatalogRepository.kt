package com.cardwise.app.data.repository

import androidx.room.withTransaction
import com.cardwise.app.data.local.BenefitCatalogDao
import com.cardwise.app.data.local.BenefitCatalogEntity
import com.cardwise.app.data.local.BenefitCatalogMetadataEntity
import com.cardwise.app.data.local.CardDatabase
import com.cardwise.app.domain.model.BenefitCatalogEntry
import com.cardwise.app.domain.model.BenefitCatalogSnapshot
import com.cardwise.app.domain.repository.BenefitCatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomBenefitCatalogRepository(private val database: CardDatabase) : BenefitCatalogRepository {
    private val dao: BenefitCatalogDao = database.benefitCatalogDao()

    override fun observeCatalog(): Flow<BenefitCatalogSnapshot> =
        combine(dao.observeEntries(), dao.observeVersion()) { entries, version ->
            BenefitCatalogSnapshot(version ?: 0L, entries.map { it.toDomain() })
        }

    override suspend fun replaceCatalog(snapshot: BenefitCatalogSnapshot) {
        require(snapshot.version >= 0L)
        database.withTransaction {
            val currentVersion = dao.getVersion()
            if (currentVersion != null && snapshot.version <= currentVersion) return@withTransaction

            dao.clearEntries()
            dao.insertEntries(snapshot.entries.map { it.toEntity(snapshot.version) })
            dao.insertMetadata(BenefitCatalogMetadataEntity(version = snapshot.version))
        }
    }

    private fun BenefitCatalogEntry.toEntity(version: Long) = BenefitCatalogEntity(
        cardId, benefitId, title, description,
        categories.sorted().joinToString("\u001f"), merchantHints.sorted().joinToString("\u001f"),
        rewardRatePercent, maxRewardAmount, minimumSpend, maximumEligibleSpend, priority, version
    )

    private fun BenefitCatalogEntity.toDomain() = BenefitCatalogEntry(
        cardId, benefitId, title, description,
        categories.split("\u001f").filter(String::isNotBlank).toSet(),
        merchantHints.split("\u001f").filter(String::isNotBlank).toSet(),
        rewardRatePercent, maxRewardAmount, minimumSpend, maximumEligibleSpend, priority
    )
}
