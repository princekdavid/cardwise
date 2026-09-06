package com.cardwise.app.domain.repository

import com.cardwise.app.domain.model.BenefitCatalogSnapshot
import kotlinx.coroutines.flow.Flow

interface BenefitCatalogRepository {
    fun observeCatalog(): Flow<BenefitCatalogSnapshot>
    suspend fun replaceCatalog(snapshot: BenefitCatalogSnapshot)
}

/** Optional remote boundary. Implementations may be added later without coupling the app to a backend. */
interface BenefitCatalogDataSource {
    suspend fun fetchCatalog(currentVersion: Long): BenefitCatalogSnapshot?
}
