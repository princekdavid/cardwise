package com.cardwise.app.ui

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cardwise.app.MainActivity
import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.repository.CardRepository
import com.cardwise.app.domain.scan.UpiPaymentRequest
import com.cardwise.app.domain.rewards.RewardRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardWiseAppTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

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

    @Test
    fun scannedPayment_flowsThroughRecommendationToHandoff() {
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.Launched)
        val card = Card(
            id = 1L,
            issuer = "CardWise Bank",
            name = "Everyday Rewards",
            lastFour = "1234",
            network = CardNetwork.VISA
        )
        val payment = UpiPaymentRequest(
            vpa = "merchant@upi",
            merchantName = "CardWise Shop",
            amount = BigDecimal("125.00"),
            currency = "INR",
            transactionReference = "ref-123",
            note = "Order 42"
        )

        composeRule.setContent {
            CardWiseApp(
                repository = FakeCardRepository(listOf(card)),
                recommendationRules = mapOf(
                    card.id to listOf(RewardRule("dining", rewardRatePercent = 5.0))
                ),
                paymentLauncher = launcher,
                initialPayment = payment
            )
        }

        composeRule.onNodeWithText("CardWise Shop").assertExists()
        composeRule.onNodeWithText("₹125.00").assertExists()
        captureScreenshot("01-after-launch")

        composeRule.onNodeWithTag("recommendation_category").performTextInput("dining")
        captureScreenshot("02-after-category")

        try {
            composeRule.waitUntil(timeoutMillis = 30_000) {
                composeRule.onAllNodesWithText("Everyday Rewards").fetchSemanticsNodes().isNotEmpty() &&
                    composeRule.onAllNodesWithText("Continue to UPI app").fetchSemanticsNodes().isNotEmpty()
            }
        } catch (failure: Throwable) {
            captureScreenshot("03-recommendation-wait-failure")
            throw failure
        }

        captureScreenshot("03-recommendation-ready")
        composeRule.onNodeWithText("Continue to UPI app").performScrollTo()
        composeRule.onNodeWithText("Continue to UPI app").assertExists()
        composeRule.onNodeWithText("Continue to UPI app").performClick()

        composeRule.onNodeWithText("Continue to your UPI app?").assertExists()
        captureScreenshot("04-handoff-dialog")
        composeRule.onNodeWithText("Continue").performClick()
        captureScreenshot("05-after-handoff")

        assert(launcher.launchCount == 1)
        composeRule.onAllNodesWithText("Continue to UPI app").assertCountEquals(0)
    }
}

private fun captureScreenshot(name: String) {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val screenshotDirectory = File(
        instrumentation.targetContext.getExternalFilesDir(null),
        "ui-screenshots"
    ).apply { mkdirs() }
    val screenshot = instrumentation.uiAutomation.takeScreenshot()
    FileOutputStream(File(screenshotDirectory, "$name.png")).use { output ->
        check(screenshot.compress(Bitmap.CompressFormat.PNG, 100, output)) {
            "Failed to encode screenshot: $name"
        }
    }
    screenshot.recycle()
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
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.Launched)
        composeRule.setContent {
            PaymentHandoffDialog(
                payment = payment,
                launcher = launcher,
                onDismiss = {},
                onHandoffCompleted = {}
            )
        }

        composeRule.onNodeWithText("Continue to your UPI app?").assertExists()
        composeRule.onNodeWithText("Cancel").performClick()

        assert(launcher.launchCount == 0)
    }

    @Test
    fun continue_launchesExactlyOnceAndDismisses() {
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.Launched)
        composeRule.setContent {
            var visible by remember { mutableStateOf(true) }
            if (visible) {
                PaymentHandoffDialog(
                    payment = payment,
                    launcher = launcher,
                    onDismiss = { visible = false },
                    onHandoffCompleted = {}
                )
            }
        }

        composeRule.onNodeWithText("Continue").performClick()

        assert(launcher.launchCount == 1)
        composeRule.onAllNodesWithText("Continue to your UPI app?").assertCountEquals(0)
    }

    @Test
    fun launched_reportsOutcome() {
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.Launched)
        var result: UpiPaymentLaunchResult? = null
        composeRule.setContent {
            PaymentHandoffDialog(
                payment = payment,
                launcher = launcher,
                onDismiss = {},
                onHandoffCompleted = { result = it }
            )
        }

        composeRule.onNodeWithText("Continue").performClick()

        assert(result == UpiPaymentLaunchResult.Launched)
    }

    @Test
    fun noUpiApp_reportsOutcome() {
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.NoUpiApp)
        var result: UpiPaymentLaunchResult? = null
        composeRule.setContent {
            PaymentHandoffDialog(
                payment = payment,
                launcher = launcher,
                onDismiss = {},
                onHandoffCompleted = { result = it }
            )
        }

        composeRule.onNodeWithText("Continue").performClick()

        assert(result == UpiPaymentLaunchResult.NoUpiApp)
    }
}

private class FakeCardRepository(initialCards: List<Card>) : CardRepository {
    private val cards = MutableStateFlow(initialCards)

    override fun observeCards(): Flow<List<Card>> = cards

    override suspend fun addCard(card: Card): Long {
        cards.value = cards.value + card
        return card.id
    }

    override suspend fun updateCard(card: Card) {
        cards.value = cards.value.map { if (it.id == card.id) card else it }
    }

    override suspend fun deleteCard(cardId: Long) {
        cards.value = cards.value.filterNot { it.id == cardId }
    }
}

private class RecordingLauncher(
    private val result: UpiPaymentLaunchResult
) : UpiPaymentLauncher {
    var launchCount = 0
        private set

    override fun launch(payment: UpiPaymentRequest): UpiPaymentLaunchResult {
        launchCount += 1
        return result
    }
}
