package com.cardwise.app

import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream
import org.junit.Rule
import org.junit.Test

class MainActivitySmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivity_launchesAndRendersCardWiseUi() {
        composeRule.onNodeWithText("Wallet").assertExists()
        composeRule.onNodeWithText("Scan").assertExists()
        composeRule.onNodeWithText("Insights").assertExists()

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val directory = File(
            instrumentation.targetContext.getExternalFilesDir(null),
            "ui-screenshots"
        ).apply { mkdirs() }
        val screenshot = instrumentation.uiAutomation.takeScreenshot()
        FileOutputStream(File(directory, "00-main-activity.png")).use { output ->
            check(screenshot.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
        screenshot.recycle()
    }
}
