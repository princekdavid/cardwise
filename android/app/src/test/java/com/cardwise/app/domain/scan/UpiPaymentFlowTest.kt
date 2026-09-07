package com.cardwise.app.domain.scan

import com.cardwise.app.domain.model.BenefitCatalogEntry
import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.recommendation.MerchantBenefitMatcher
import com.cardwise.app.domain.recommendation.PaymentContext
import com.cardwise.app.domain.recommendation.RecommendationEngine
import com.cardwise.app.domain.rewards.RewardRule
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpiPaymentFlowTest {
    @Test
    fun parsedQrFlowsThroughMerchantMatchingRecommendationAndSanitizedHandoff() {
        val rawPayload = "upi://pay?pa=merchant@upi&pn=Fresh%20Mart&am=1250.50&cu=INR&tr=order-42&tn=Weekly%20groceries&foo=secret"

        val payment = (UpiQrParser.parse(rawPayload) as UpiQrParseResult.Success).payment
        val catalogEntry = BenefitCatalogEntry(
            cardId = 7L,
            benefitId = "fresh-mart",
            title = "Fresh Mart rewards",
            description = "Extra rewards at Fresh Mart",
            categories = setOf("Groceries"),
            merchantHints = setOf("fresh mart"),
            rewardRatePercent = 5.0
        )

        val match = MerchantBenefitMatcher.match(
            entries = listOf(catalogEntry),
            merchantName = payment.merchantName,
            vpa = payment.vpa
        ).single()
        val category = match.benefit.categories.single()

        val card = Card(
            id = 7L,
            issuer = "Test Bank",
            name = "Rewards Card",
            lastFour = "1234",
            network = CardNetwork.VISA
        )
        val recommendations = RecommendationEngine.recommend(
            context = PaymentContext(category = category, amount = payment.amount!!.toDouble()),
            cards = listOf(card),
            rules = mapOf(
                card.id to listOf(
                    RewardRule(category = category, rewardRatePercent = 5.0)
                )
            )
        )

        assertEquals("merchant@upi", payment.vpa)
        assertEquals("Fresh Mart", payment.merchantName)
        assertEquals(BigDecimal("1250.50"), payment.amount)
        assertEquals("Groceries", category)
        assertEquals(1, recommendations.size)
        assertEquals(1, recommendations.single().rank)
        assertEquals(62.525, recommendations.single().reward.estimatedReward, 0.000001)

        val handoffUri = UpiPaymentHandoff.buildUri(payment)
        assertTrue(handoffUri.startsWith("upi://pay?"))
        assertTrue(handoffUri.contains("pa=merchant%40upi"))
        assertTrue(handoffUri.contains("pn=Fresh+Mart"))
        assertTrue(handoffUri.contains("am=1250.5"))
        assertTrue(handoffUri.contains("cu=INR"))
        assertTrue(handoffUri.contains("tr=order-42"))
        assertTrue(handoffUri.contains("tn=Weekly+groceries"))
        assertFalse(handoffUri.contains("foo"))
        assertFalse(handoffUri.contains("secret"))
        assertFalse(handoffUri.contains(rawPayload))
    }
}
