package com.cardwise.app.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cardwise.app.domain.scan.UpiPaymentRequest
import java.math.BigDecimal
import org.junit.Rule
import org.junit.Test

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
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.Launched)
        composeRule.setContent { PaymentHandoffDialog(payment, launcher, {}, {}) }
        composeRule.onNodeWithText("Continue to your UPI app?").assertExists()
        composeRule.onNodeWithText("Cancel").performClick()
        assert(launcher.launchCount == 0)
    }

    @Test
    fun continue_launchesExactlyOnceAndDismisses() {
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.Launched)
        composeRule.setContent {
            var visible by remember { mutableStateOf(true) }
            if (visible) PaymentHandoffDialog(payment, launcher, { visible = false }, {})
        }
        composeRule.onNodeWithText("Continue").performClick()
        assert(launcher.launchCount == 1)
        composeRule.onAllNodesWithText("Continue to your UPI app?").assertCountEquals(0)
    }

    @Test
    fun launched_reportsOutcome() {
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.Launched)
        var result: UpiPaymentLaunchResult? = null
        composeRule.setContent { PaymentHandoffDialog(payment, launcher, {}, { result = it }) }
        composeRule.onNodeWithText("Continue").performClick()
        assert(result == UpiPaymentLaunchResult.Launched)
    }

    @Test
    fun noUpiApp_reportsOutcome() {
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.NoUpiApp)
        var result: UpiPaymentLaunchResult? = null
        composeRule.setContent { PaymentHandoffDialog(payment, launcher, {}, { result = it }) }
        composeRule.onNodeWithText("Continue").performClick()
        assert(result == UpiPaymentLaunchResult.NoUpiApp)
    }
}

private class RecordingLauncher(private val result: UpiPaymentLaunchResult) : UpiPaymentLauncher {
    var launchCount = 0
        private set
    override fun launch(payment: UpiPaymentRequest): UpiPaymentLaunchResult {
        launchCount += 1
        return result
    }
}
