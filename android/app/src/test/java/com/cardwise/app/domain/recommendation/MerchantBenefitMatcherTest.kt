package com.cardwise.app.domain.recommendation

import com.cardwise.app.domain.model.BenefitCatalogEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MerchantBenefitMatcherTest {
    @Test
    fun match_prefersExactMerchantName() {
        val exact = entry("exact", hints = setOf("Acme Cafe"), priority = 1)
        val broad = entry("broad", hints = setOf("cafe"), priority = 10)

        val matches = MerchantBenefitMatcher.match(listOf(broad, exact), "  ACME CAFE ", "store@upi")

        assertEquals("exact", matches.first().benefit.benefitId)
        assertEquals(100, matches.first().score)
    }

    @Test
    fun match_canUseVpaWhenMerchantNameIsMissing() {
        val benefit = entry("vpa", hints = setOf("acmestore@upi"))

        val matches = MerchantBenefitMatcher.match(listOf(benefit), null, "acmestore@upi")

        assertEquals(1, matches.size)
        assertEquals(90, matches.single().score)
    }

    @Test
    fun match_returnsStableOrderingForEqualScores() {
        val second = entry("b", hints = setOf("store"), priority = 1)
        val first = entry("a", hints = setOf("store"), priority = 1)

        val matches = MerchantBenefitMatcher.match(listOf(second, first), "My Store", "store@upi")

        assertEquals(listOf("a", "b"), matches.map { it.benefit.benefitId })
    }

    @Test
    fun match_ignoresUnmatchedHints() {
        val benefit = entry("none", hints = setOf("unknown"))

        val matches = MerchantBenefitMatcher.match(listOf(benefit), "Acme Cafe", "acme@upi")

        assertTrue(matches.isEmpty())
    }

    @Test
    fun match_returnsEmptyWhenPaymentMetadataIsMissing() {
        val benefit = entry("none", hints = setOf("acme"))

        assertTrue(MerchantBenefitMatcher.match(listOf(benefit), null, null).isEmpty())
    }

    private fun entry(
        id: String,
        hints: Set<String>,
        priority: Int = 0
    ) = BenefitCatalogEntry(
        cardId = 1L,
        benefitId = id,
        title = id,
        description = "Benefit $id",
        merchantHints = hints,
        priority = priority
    )
}
