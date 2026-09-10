package com.cardwise.app.domain.offers

import com.cardwise.app.domain.resource.ResourceStore
import com.cardwise.app.domain.resource.ResourceSyncEngine
import com.cardwise.app.domain.resource.ResourceSyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Deterministic, explainable offer eligibility and benefit calculation. */
class OfferEngine(
    providers: List<OfferProvider>,
    private val store: ResourceStore<Offer>
) {
    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: Flow<List<Offer>> = _offers.asStateFlow()
    private val syncEngine = ResourceSyncEngine(providers, store)

    suspend fun loadCached(): List<Offer> {
        val cached = store.read()
        _offers.value = cached
        return cached
    }

    suspend fun refresh(): ResourceSyncResult {
        val result = syncEngine.refresh()
        _offers.value = store.read()
        return result
    }

    fun evaluate(context: OfferEvaluationContext): List<OfferEvaluation> =
        _offers.value
            .map { evaluateOffer(it, context) }
            .filter(OfferEvaluation::eligible)
            .sortedWith(compareByDescending<OfferEvaluation> { it.estimatedBenefit }.thenBy { it.offer.offerId })

    fun evaluateAll(context: OfferEvaluationContext): List<OfferEvaluation> =
        _offers.value
            .map { evaluateOffer(it, context) }
            .sortedWith(compareByDescending<OfferEvaluation> { it.eligible }.thenByDescending { it.estimatedBenefit }.thenBy { it.offer.offerId })

    private fun evaluateOffer(offer: Offer, context: OfferEvaluationContext): OfferEvaluation {
        val now = context.now
        val beforeStart = offer.metadata.effectiveFrom?.let(now::isBefore) == true
        val expired = offer.metadata.expiresAt?.let { !now.isBefore(it) } == true
        val merchantMismatch = context.merchantId != null && offer.merchantId != "*" && !offer.merchantId.equals(context.merchantId, ignoreCase = true)
        val categoryMismatch = context.category != null && offer.category != null && !offer.category.equals(context.category, ignoreCase = true)
        val cardMismatch = offer.applicableProductIds.isNotEmpty() && context.cardProductIds.isNotEmpty() && offer.applicableProductIds.intersect(context.cardProductIds).isEmpty()
        val missingCardContext = offer.applicableProductIds.isNotEmpty() && context.cardProductIds.isEmpty()
        val belowMinimum = offer.minimumSpend?.let { minimum -> context.amount?.let { it < minimum } } == true
        val reason = when {
            beforeStart -> "Offer has not started yet."
            expired -> "Offer has expired."
            merchantMismatch -> "Offer does not apply to this merchant."
            categoryMismatch -> "Offer does not apply to this category."
            cardMismatch -> "No enrolled eligible card matches this offer."
            missingCardContext -> "Eligible card information is required to confirm this offer."
            belowMinimum -> "Transaction is below the minimum spend."
            else -> "Eligible for this payment context."
        }
        val eligible = reason == "Eligible for this payment context."
        return OfferEvaluation(offer, eligible, if (eligible) calculateBenefit(offer, context.amount) else 0.0, reason)
    }

    private fun calculateBenefit(offer: Offer, amount: Double?): Double {
        val spend = amount ?: return 0.0
        val raw = when (val benefit = offer.benefit) {
            is OfferBenefit.PercentageDiscount -> spend * benefit.percent / 100.0
            is OfferBenefit.PercentageCashback -> spend * benefit.percent / 100.0
            is OfferBenefit.FlatDiscount -> benefit.amount
            is OfferBenefit.FlatCashback -> benefit.amount
        }
        return minOf(raw, offer.maximumBenefit ?: raw)
    }
}

data class OfferEvaluationContext(
    val merchantId: String? = null,
    val category: String? = null,
    val amount: Double? = null,
    val cardProductIds: Set<String> = emptySet(),
    val now: java.time.Instant = java.time.Instant.now()
)

data class OfferEvaluation(
    val offer: Offer,
    val eligible: Boolean,
    val estimatedBenefit: Double,
    val reason: String
)
