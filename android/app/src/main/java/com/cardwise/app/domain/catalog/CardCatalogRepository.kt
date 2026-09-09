package com.cardwise.app.domain.catalog

import kotlinx.coroutines.flow.Flow

/** Stable boundary between catalog consumers and dynamic card-product providers/cache. */
interface CardCatalogRepository {
    val products: Flow<List<CardProduct>>
    suspend fun loadCached()
    suspend fun refresh(): CardCatalogRefreshResult
}

sealed interface CardCatalogRefreshResult {
    data class Success(
        val productCount: Int,
        val providerCount: Int,
        val providerFailures: Int
    ) : CardCatalogRefreshResult

    data class Unavailable(val reason: String) : CardCatalogRefreshResult
}

class DefaultCardCatalogRepository(
    private val engine: CardCatalogueEngine
) : CardCatalogRepository {
    override val products: Flow<List<CardProduct>> = engine.products

    override suspend fun loadCached() = engine.loadCached()

    override suspend fun refresh(): CardCatalogRefreshResult = when (val result = engine.refresh()) {
        is com.cardwise.app.domain.resource.ResourceSyncResult.Success ->
            CardCatalogRefreshResult.Success(result.itemCount, result.providerCount, result.failures.size)
        is com.cardwise.app.domain.resource.ResourceSyncResult.Failed ->
            CardCatalogRefreshResult.Unavailable("No catalogue provider is currently available")
        com.cardwise.app.domain.resource.ResourceSyncResult.NoProviders ->
            CardCatalogRefreshResult.Unavailable("No catalogue provider is configured")
    }
}
