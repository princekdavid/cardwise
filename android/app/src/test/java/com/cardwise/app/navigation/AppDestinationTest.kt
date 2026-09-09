package com.cardwise.app.navigation

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppDestinationTest {
    @Test
    fun destinations_haveExpectedOrder() {
        assertEquals(
            listOf(
                AppDestination.Cockpit,
                AppDestination.Wallet,
                AppDestination.Scan,
                AppDestination.Reasoning,
                AppDestination.Offers,
                AppDestination.Recommendation
            ),
            AppDestination.entries
        )
    }

    @Test
    fun destinations_haveUserFacingLabels() {
        assertEquals("Cockpit", AppDestination.Cockpit.label)
        assertEquals("Cards", AppDestination.Wallet.label)
        assertEquals("Scan", AppDestination.Scan.label)
        assertEquals("Reasoning", AppDestination.Reasoning.label)
        assertEquals("Offers", AppDestination.Offers.label)
        assertEquals("Best Way", AppDestination.Recommendation.label)
    }

    @Test
    fun onlyPrimaryDestinations_showInBottomBar() {
        assertTrue(AppDestination.Cockpit.showInBottomBar)
        assertTrue(AppDestination.Wallet.showInBottomBar)
        assertTrue(AppDestination.Scan.showInBottomBar)
        assertTrue(AppDestination.Offers.showInBottomBar)
        assertTrue(!AppDestination.Reasoning.showInBottomBar)
        assertTrue(!AppDestination.Recommendation.showInBottomBar)
    }

    @Test
    fun destinationLabels_areNotBlank() {
        assertTrue(AppDestination.entries.all { it.label.isNotBlank() })
    }
}
