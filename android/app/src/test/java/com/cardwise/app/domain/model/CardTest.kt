package com.cardwise.app.domain.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CardTest {
    @Test
    fun card_defaultsToActiveWithNoBenefits() {
        val card = Card(1L, "Issuer", "Rewards", "1234", CardNetwork.VISA)

        assertTrue(card.isActive)
        assertTrue(card.benefits.isEmpty())
    }

    @Test
    fun card_preservesSafeDisplayFields() {
        val card = Card(7L, "Bank", "Travel", "9876", CardNetwork.RUPAY)

        assertEquals(7L, card.id)
        assertEquals("9876", card.lastFour)
        assertEquals(CardNetwork.RUPAY, card.network)
    }

    @Test
    fun inactiveCard_isRepresentedExplicitly() {
        val card = Card(2L, "Bank", "Cashback", "0001", CardNetwork.MASTERCARD, isActive = false)

        assertFalse(card.isActive)
    }
}
