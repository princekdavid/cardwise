package com.cardwise.app.navigation

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppDestinationTest {
    @Test
    fun destinations_haveExpectedOrder() {
        assertEquals(
            listOf(AppDestination.Wallet, AppDestination.Scan, AppDestination.Insights),
            AppDestination.entries
        )
    }

    @Test
    fun destinations_haveUserFacingLabels() {
        assertEquals("Wallet", AppDestination.Wallet.label)
        assertEquals("Scan", AppDestination.Scan.label)
        assertEquals("Insights", AppDestination.Insights.label)
    }

    @Test
    fun destinationLabels_areNotBlank() {
        assertTrue(AppDestination.entries.all { it.label.isNotBlank() })
    }
}
