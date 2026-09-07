package com.cardwise.app.ui

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class CardWiseAppTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun wallet_isSelectedInitially() {
        composeRule.setContent { CardWiseApp() }
        composeRule.onNodeWithText("Wallet").assertIsSelected()
    }

    @Test
    fun selectingScan_updatesSelectedDestination() {
        composeRule.setContent { CardWiseApp() }
        composeRule.onNodeWithText("Scan").performClick()
        composeRule.onNodeWithText("Scan").assertIsSelected()
        composeRule.onNodeWithText("Wallet").assertIsNotSelected()
    }

    @Test
    fun selectingScan_withoutPermission_showsPrivacyFirstCameraPrompt() {
        composeRule.setContent { CardWiseApp() }
        composeRule.onNodeWithText("Scan").performClick()
        composeRule.onNodeWithText("Camera access needed").assertExists()
        composeRule.onNodeWithText(
            "The QR payload is processed locally and is not stored.",
            substring = true
        ).assertExists()
    }

    @Test
    fun selectingInsights_updatesSelectedDestination() {
        composeRule.setContent { CardWiseApp() }
        composeRule.onNodeWithText("Insights").performClick()
        composeRule.onNodeWithText("Insights").assertIsSelected()
    }
}
