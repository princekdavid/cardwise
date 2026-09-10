package com.cardwise.app.domain.offers

import com.cardwise.app.domain.resource.ResourceSyncResult
import kotlinx.coroutines.flow.Flow

class DefaultOfferCatalogRepository(
    private val engine: OfferEngine
) : OfferCatalogRepository {
    override val offers: Flow<List<Offer>> = engine.offers
    override suspend fun loadCached(): List<Offer> = engine.loadCached()
    override suspend fun refresh(): ResourceSyncResult = engine.refresh()
    override fun evaluate(context: OfferEvaluationContext): List<OfferEvaluation> = engine.evaluate(context)
    override fun evaluateAll(context: OfferEvaluationContext): List<OfferEvaluation> = engine.evaluateAll(context)
}

interface OfferCatalogRepository {
    val offers: Flow<List<Offer>>
    suspend fun loadCached(): List<Offer>
    suspend fun refresh(): ResourceSyncResult
    fun evaluate(context: OfferEvaluationContext): List<OfferEvaluation>
    fun evaluateAll(context: OfferEvaluationContext): List<OfferEvaluation>
}
