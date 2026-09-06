package com.cardwise.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BenefitCatalogDao {
    @Query("SELECT * FROM benefit_catalog ORDER BY cardId, priority DESC, benefitId")
    fun observeEntries(): Flow<List<BenefitCatalogEntity>>

    @Query("SELECT version FROM benefit_catalog_metadata WHERE id = 1")
    fun observeVersion(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<BenefitCatalogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: BenefitCatalogMetadataEntity)

    @Query("DELETE FROM benefit_catalog")
    suspend fun clearEntries()
}
