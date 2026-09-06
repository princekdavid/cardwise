package com.cardwise.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cardwise.app.domain.scan.UpiPaymentRequest
import java.math.BigDecimal
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
        composeRule.onNodeWithText("The QR payload is processed locally and is not stored.").assertExists()
    }

    @Test
    fun selectingInsights_updatesSelectedDestination() {
        composeRule.setContent { CardWiseApp() }

        composeRule.onNodeWithText("Insights").performClick()

        composeRule.onNodeWithText("Insights").assertIsSelected()
    }
}

@RunWith(AndroidJUnit4::class)
class PaymentHandoffDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val payment = UpiPaymentRequest(
        vpa = "merchant@upi",
        merchantName = "CardWise Shop",
        amount = BigDecimal("125.00"),
        currency = "INR",
        transactionReference = "ref-123",
        note = "Order 42"
    )

    @Test
    fun cancel_doesNotLaunchPayment() {
        val launcher = RecordingLauncher()
        composeRule.setContent {
            PaymentHandoffDialog(
                payment = payment,
                launcher = launcher,
                onDismiss = {},
                onHandoffAttempted = {}
            )
        }

        composeRule.onNodeWithText("Continue to your UPI app?").assertExists()
        composeRule.onNodeWithText("Cancel").performClick()

        assert(launcher.launchCount == 0)
    }

    @Test
    fun continue_launchesExactlyOnceAndDismisses() {
        val launcher = RecordingLauncher()
        composeRule.setContent {
            var visible by remember { mutableStateOf(true) }
            if (visible) {
                PaymentHandoffDialog(
                    payment = payment,
                    launcher = launcher,
                    onDismiss = { visible = false },
                    onHandoffAttempted = {}
                )
            }
        }

        composeRule.onNodeWithText("Continue").performClick()

        assert(launcher.launchCount == 1)
        composeRule.onNodeWithText("Continue to your UPI app?").assertDoesNotExist()
    }

    private class RecordingLauncher : UpiPaymentLauncher {
        var launchCount = 0
            private set

        override fun launch(payment: UpiPaymentRequest): UpiPaymentLaunchResult {
            launchCount += 1
            return UpiPaymentLaunchResult.Launched
        }
    }
}
