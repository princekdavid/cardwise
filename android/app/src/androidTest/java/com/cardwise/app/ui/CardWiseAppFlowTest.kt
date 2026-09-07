package com.cardwise.app.ui

import android.graphics.Bitmap
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream
import org.junit.Rule
import org.junit.Test

class CardWiseAppFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<CardWiseFlowActivity>()

    @Test
    fun scannedPayment_flowsThroughRecommendationToHandoff() {
        composeRule.onNodeWithText("CardWise Shop").assertExists()
        composeRule.onNodeWithText("₹125.00").assertExists()
        captureScreenshot("01-flow-launch")

        composeRule.onNodeWithTag("recommendation_category").performTextInput("dining")
        captureScreenshot("02-flow-category")

        try {
            composeRule.waitUntil(timeoutMillis = 30_000) {
                composeRule.onAllNodesWithText("Everyday Rewards").fetchSemanticsNodes().isNotEmpty() &&
                    composeRule.onAllNodesWithText("Continue to UPI app").fetchSemanticsNodes().isNotEmpty()
            }
        } catch (failure: Throwable) {
            captureScreenshot("03-flow-recommendation-failure")
            throw failure
        }

        captureScreenshot("03-flow-recommendation-ready")
        composeRule.onNodeWithText("Continue to UPI app").performScrollTo()
        composeRule.onNodeWithText("Continue to UPI app").performClick()
        composeRule.onNodeWithText("Continue to your UPI app?").assertExists()
        captureScreenshot("04-flow-handoff-dialog")

        composeRule.onNodeWithText("Continue").performClick()
        captureScreenshot("05-flow-after-handoff")
        assert(CardWiseFlowActivity.launcher.launchCount == 1)
        composeRule.onAllNodesWithText("Continue to UPI app").assertCountEquals(0)
    }

    private fun captureScreenshot(name: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val screenshotDirectory = File(
            instrumentation.targetContext.getExternalFilesDir(null),
            "ui-screenshots"
        ).apply { mkdirs() }
        val screenshot = instrumentation.uiAutomation.takeScreenshot()
        FileOutputStream(File(screenshotDirectory, "$name.png")).use { output ->
            check(screenshot.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
        screenshot.recycle()
    }
}
