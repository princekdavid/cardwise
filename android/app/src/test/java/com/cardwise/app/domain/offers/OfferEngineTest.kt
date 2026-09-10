package com.cardwise.app.domain.offers

import com.cardwise.app.domain.resource.ResourceBatch
import com.cardwise.app.domain.resource.ResourceConfidence
import com.cardwise.app.domain.resource.ResourceMetadata
import com.cardwise.app.domain.resource.ResourceSourceType
import com.cardwise.app.domain.resource.ResourceStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class OfferEngineTest {
    private val metadata = ResourceMetadata(
        sourceId = "test",
        sourceType = ResourceSourceType.UNKNOWN,
        confidence = ResourceConfidence.UNKNOWN
    )

    @Test
    fun percentageBenefit_isCappedAndMinimumSpendIsEnforced() = runBlocking {
        val offer = offer(OfferBenefit.PercentageCashback(10.0), minimumSpend = 1000.0, maximumBenefit = 150.0)
        val engine = engine(offer)
        engine.refresh()

        val below = engine.evaluate(OfferEvaluationContext(amount = 999.0, now = Instant.parse("2026-09-10T00:00:00Z")))
        val eligible = engine.evaluate(OfferEvaluationContext(amount = 2000.0, now = Instant.parse("2026-09-10T00:00:00Z")))

        assertTrue(below.isEmpty())
        assertEquals(150.0, eligible.single().estimatedBenefit, 0.001)
    }

    @Test
    fun expiredOffer_isNotEligible() = runBlocking {
        val expired = offer(OfferBenefit.FlatCashback(100.0), expiresAt = Instant.parse("2026-09-01T00:00:00Z"))
        val engine = engine(expired)
        engine.refresh()

        assertTrue(engine.evaluate(OfferEvaluationContext(amount = 5000.0, now = Instant.parse("2026-09-10T00:00:00Z"))).isEmpty())
        assertEquals("Offer has expired.", engine.evaluateAll(OfferEvaluationContext(amount = 5000.0, now = Instant.parse("2026-09-10T00:00:00Z"))).single().reason)
    }

    @Test
    fun eligibleOffers_areSortedByBenefitThenStableId() = runBlocking {
        val low = offer(OfferBenefit.FlatCashback(50.0), id = "b")
        val high = offer(OfferBenefit.FlatCashback(100.0), id = "a")
        val engine = engine(low, high)
        engine.refresh()

        val result = engine.evaluate(OfferEvaluationContext(amount = 1000.0, now = Instant.parse("2026-09-10T00:00:00Z")))

        assertEquals(listOf("a", "b"), result.map { it.offer.offerId })
    }

    private fun engine(vararg offers: Offer): OfferEngine = OfferEngine(
        providers = listOf(FakeProvider(offers.toList(), metadata)),
        store = FakeStore()
    )

    private fun offer(
        benefit: OfferBenefit,
        id: String = "offer",
        minimumSpend: Double? = null,
        maximumBenefit: Double? = null,
        expiresAt: Instant? = null
    ) = Offer(
        offerId = id,
        merchantId = "*",
        merchantName = "Test Merchant",
        title = "Test offer",
        description = "Test",
        category = "Dining",
        benefit = benefit,
        minimumSpend = minimumSpend,
        maximumBenefit = maximumBenefit,
        metadata = metadata.copy(expiresAt = expiresAt)
    )

    private class FakeProvider(
        private val offers: List<Offer>,
        private val metadata: ResourceMetadata
    ) : OfferProvider {
        override val providerId: String = "test-provider"
        override suspend fun fetch(): ResourceBatch<Offer> = ResourceBatch(offers, metadata, 1L)
    }

    private class FakeStore : ResourceStore<Offer> {
        private var cached: List<Offer> = emptyList()
        override suspend fun replace(batch: ResourceBatch<Offer>) { cached = batch.items }
        override suspend fun read(): List<Offer> = cached
    }
}
