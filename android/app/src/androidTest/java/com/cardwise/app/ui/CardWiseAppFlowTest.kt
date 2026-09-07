package com.cardwise.app.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class CardWiseAppFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<CardWiseFlowActivity>()

    @Test
    fun cardCreation_thenScannedPayment_flowsThroughRecommendationToHandoff() {
        composeRule.onNodeWithText("Your cards").assertExists()
        composeRule.onNodeWithText("No cards yet").assertExists()
        captureScreenshot("01-wallet-empty")

        composeRule.onNodeWithText("Add your first card").performClick()
        composeRule.onNodeWithTag("card_issuer").performTextInput("CardWise Bank")
        composeRule.onNodeWithTag("card_name").performTextInput("Everyday Rewards")
        composeRule.onNodeWithTag("card_last_four").performTextInput("1234")
        composeRule.onNodeWithText("Network: Other").performClick()
        composeRule.onNodeWithText("Visa").performClick()
        captureScreenshot("02-card-form-filled")

        composeRule.onNodeWithText("Save card").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Everyday Rewards").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Everyday Rewards").assertExists()
        captureScreenshot("03-card-saved")

        composeRule.onNodeWithText("Scan").performClick()
        captureScreenshot("04-scan-screen")

        composeRule.activity.showScannedPayment()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("CardWise Shop").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("CardWise Shop").assertExists()
        composeRule.onNodeWithText("₹125.00").assertExists()
        captureScreenshot("05-scanned-payment")

        composeRule.onNodeWithTag("recommendation_category").performTextInput("dining")
        composeRule.onNodeWithTag("recommendation_category").performImeAction()
        captureScreenshot("06-category-entered")

        try {
            composeRule.waitUntil(timeoutMillis = 30_000) {
                composeRule.onAllNodesWithText("Everyday Rewards").fetchSemanticsNodes().isNotEmpty() &&
                    composeRule.onAllNodesWithText("Continue to UPI app").fetchSemanticsNodes().isNotEmpty()
            }
        } catch (failure: Throwable) {
            captureScreenshot("07-recommendation-failure")
            throw failure
        }

        captureScreenshot("07-recommendation-ready")
        composeRule.onNodeWithText("Continue to UPI app").performClick()
        composeRule.onNodeWithText("Continue to your UPI app?").assertExists()
        captureScreenshot("08-handoff-dialog")

        composeRule.onNodeWithText("Continue").performClick()
        captureScreenshot("09-after-handoff")
        assert(CardWiseFlowActivity.launcher.launchCount == 1)
        composeRule.onAllNodesWithText("Continue to UPI app").assertCountEquals(0)
    }

    private fun captureScreenshot(name: String) {
        val instrumentation = androidx.test.InstrumentationRegistry.getInstrumentation()
        val path = "/data/local/tmp/cardwise-$name.png"
        val fd = instrumentation.uiAutomation.executeShellCommand("screencap -p $path")
        fd.close()
    }
}
