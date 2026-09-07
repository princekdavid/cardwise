package com.cardwise.app.domain.repository

import kotlinx.coroutines.flow.first

/** Coordinates an optional remote refresh while keeping local persistence authoritative. */
class BenefitCatalogSync(
    private val repository: BenefitCatalogRepository,
    private val dataSource: BenefitCatalogDataSource
) {
    suspend fun refresh(): Result {
        val current = repository.observeCatalog().first()
        val remote = dataSource.fetchCatalog(current.version) ?: return Result.NoUpdate
        if (remote.version <= current.version) return Result.NoUpdate

        repository.replaceCatalog(remote)
        return Result.Updated(remote.version)
    }

    sealed interface Result {
        data class Updated(val version: Long) : Result
        data object NoUpdate : Result
    }
}
