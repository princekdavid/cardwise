package com.cardwise.app.ui

import android.accessibilityservice.AccessibilityService
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class CardWiseAppFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<CardWiseFlowActivity>()

    @Test
    fun cardCreation_thenScannedPayment_flowsThroughRecommendationToHandoff() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Your cards").fetchSemanticsNodes().isNotEmpty() &&
                composeRule.onAllNodesWithText("No cards yet").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Your cards").assertExists()
        composeRule.onNodeWithText("No cards yet").assertExists()
        captureScreenshot("01-wallet-empty")

        composeRule.onNodeWithText("Add your first card").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("card_issuer").fetchSemanticsNodes().isNotEmpty()
        }
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
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Scan").fetchSemanticsNodes().isNotEmpty()
        }
        captureScreenshot("04-scan-screen")

        composeRule.activity.showScannedPayment()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("CardWise Shop").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("CardWise Shop").assertExists()
        composeRule.onNodeWithText("₹125.00").assertExists()
        captureScreenshot("05-scanned-payment")

        composeRule.onNodeWithTag("recommendation_category").performTextInput("dining")
        composeRule.waitForIdle()
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        composeRule.waitForIdle()
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
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val path = "/data/local/tmp/cardwise-$name.png"
        val fd: ParcelFileDescriptor = instrumentation.uiAutomation.executeShellCommand("screencap -p $path")
        ParcelFileDescriptor.AutoCloseInputStream(fd).use { it.readBytes() }
    }
}
