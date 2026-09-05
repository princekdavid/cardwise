package com.cardwise.app.domain.validation

import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardBenefit
import com.cardwise.app.domain.model.CardNetwork
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CardValidatorTest {
    private val validator = CardValidator()

    @Test
    fun validCard_passesValidation() {
        val result = validator.validate(card())

        assertEquals(CardValidationResult.Valid, result)
    }

    @Test
    fun blankIssuer_isRejected() {
        val result = validator.validate(card(issuer = " "))

        assertIs<CardValidationResult.Invalid>(result)
        assertEquals(listOf(CardValidationError.IssuerRequired), result.errors)
    }

    @Test
    fun invalidLastFour_isRejected() {
        val result = validator.validate(card(lastFour = "12A4"))

        assertIs<CardValidationResult.Invalid>(result)
        assertEquals(listOf(CardValidationError.LastFourInvalid), result.errors)
    }

    @Test
    fun incompleteBenefit_isRejected() {
        val result = validator.validate(
            card(benefits = listOf(CardBenefit("", "10% cashback")))
        )

        assertIs<CardValidationResult.Invalid>(result)
        assertEquals(listOf(CardValidationError.BenefitIncomplete), result.errors)
    }

    private fun card(
        issuer: String = "Bank",
        name: String = "Rewards",
        lastFour: String = "1234",
        benefits: List<CardBenefit> = emptyList()
    ) = Card(0L, issuer, name, lastFour, CardNetwork.VISA, benefits = benefits)
}
