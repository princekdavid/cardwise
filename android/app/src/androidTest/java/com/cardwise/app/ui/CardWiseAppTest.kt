package com.cardwise.app.ui

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.repository.CardRepository
import com.cardwise.app.domain.scan.UpiPaymentRequest
import com.cardwise.app.domain.rewards.RewardRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigDecimal
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardWiseAppTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun cockpit_isSelectedInitially() { composeRule.setContent { CardWiseApp() }; composeRule.onNodeWithText("Cockpit").assertIsSelected() }

    @Test fun selectingCards_updatesSelectedDestination() {
        composeRule.setContent { CardWiseApp() }
        composeRule.onNodeWithTag("nav_wallet").performClick()
        composeRule.onNodeWithTag("nav_wallet").assertIsSelected()
        composeRule.onNodeWithText("My Physical Deck").assertExists()
    }

    @Test fun emptyDeck_showsCatalogueEntryPoint() {
        composeRule.setContent { CardWiseApp(repository = FakeCardRepository(emptyList())) }
        composeRule.onNodeWithTag("nav_wallet").performClick()
        composeRule.onNodeWithText("No cards in your deck").assertExists()
        composeRule.onNodeWithText("Browse card catalogue").assertExists()
    }

    @Test fun cardCatalogue_exposesSearchAndFilterControls() {
        composeRule.setContent { CardWiseApp(repository = FakeCardRepository(emptyList())) }
        composeRule.onNodeWithTag("nav_wallet").performClick()
        composeRule.onNodeWithText("Browse card catalogue").performClick()
        composeRule.onNodeWithTag("catalog_search").assertExists()
        composeRule.onNodeWithTag("catalog_filters").assertExists()
        composeRule.onNodeWithText("Travel").performClick()
        composeRule.onNodeWithText("Travel").assertIsSelected()
    }

    @Test fun populatedDeck_showsCardAndDetailsEntryPoint() {
        val card = Card(7L, "CardWise Bank", "Everyday Rewards", "1234", CardNetwork.VISA)
        composeRule.setContent { CardWiseApp(repository = FakeCardRepository(listOf(card))) }
        composeRule.onNodeWithTag("nav_wallet").performClick()
        composeRule.onNodeWithTag("wallet_card_7").assertExists()
        composeRule.onNodeWithText("CardWise Bank • VISA").assertExists()
        composeRule.onNodeWithText("Details").performClick()
        composeRule.onNodeWithText("Everyday Rewards").assertExists()
    }

    @Test fun activeFilter_hidesInactiveCards() {
        val active = Card(1L, "CardWise Bank", "Active Card", "1111", CardNetwork.VISA, isActive = true)
        val inactive = Card(2L, "CardWise Bank", "Paused Card", "2222", CardNetwork.MASTERCARD, isActive = false)
        composeRule.setContent { CardWiseApp(repository = FakeCardRepository(listOf(active, inactive))) }
        composeRule.onNodeWithTag("nav_wallet").performClick()
        composeRule.onNodeWithText("Active").performClick()
        composeRule.onNodeWithTag("wallet_card_1").assertExists()
        composeRule.onNodeWithTag("wallet_card_2").assertDoesNotExist()
    }

    @Test fun selectingOffers_showsOfferSurface() {
        composeRule.setContent { CardWiseApp() }
        composeRule.onNodeWithText("Offers").performClick()
        composeRule.onNodeWithText("Offers").assertIsSelected()
        composeRule.onNodeWithText("Active Offers").assertExists()
    }

    @Test fun selectingScan_withoutPermission_showsPrivacyFirstCameraPrompt() {
        composeRule.setContent { CardWiseApp() }
        composeRule.onNodeWithText("Scan").performClick()
        composeRule.onNodeWithText("Camera access needed").assertExists()
        composeRule.onNodeWithText("The QR payload is processed locally and is not stored.", substring = true).assertExists()
    }

    @Ignore("Deferred until full UI is implemented and validated against the running APK")
    @Test fun scannedPayment_flowsThroughRecommendationToHandoff() {
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.Launched)
        val card = Card(1L, "CardWise Bank", "Everyday Rewards", "1234", CardNetwork.VISA)
        val payment = UpiPaymentRequest("merchant@upi", "CardWise Shop", BigDecimal("125.00"), "INR", "ref-123", "Order 42")
        composeRule.setContent { CardWiseApp(repository = FakeCardRepository(listOf(card)), recommendationRules = mapOf(card.id to listOf(RewardRule("dining", rewardRatePercent = 5.0))), paymentLauncher = launcher, initialPayment = payment) }
        composeRule.onNodeWithText("CardWise Shop").assertExists()
        composeRule.onNodeWithText("₹125.00").assertExists()
        composeRule.onNodeWithTag("recommendation_category").performTextInput("dining")
        composeRule.waitUntil(timeoutMillis = 15_000) {
            try {
                composeRule.onNodeWithTag("continue_to_upi").assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeRule.onNodeWithText("Best match").assertExists()
        composeRule.onNodeWithTag("continue_to_upi").performClick()
        composeRule.onNodeWithText("Continue to your UPI app?").assertExists()
        composeRule.onNodeWithText("Continue").performClick()
        assert(launcher.launchCount == 1)
    }
}

@RunWith(AndroidJUnit4::class)
class PaymentHandoffDialogTest {
    @get:Rule val composeRule = createComposeRule()
    private val payment = UpiPaymentRequest("merchant@upi", "CardWise Shop", BigDecimal("125.00"), "INR", "ref-123", "Order 42")

    @Test fun cancel_doesNotLaunchPayment() {
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.Launched)
        composeRule.setContent { PaymentHandoffDialog(payment, launcher, onDismiss = {}, onHandoffCompleted = {}) }
        composeRule.onNodeWithText("Continue to your UPI app?").assertExists()
        composeRule.onNodeWithText("Cancel").performClick()
        assert(launcher.launchCount == 0)
    }

    @Test fun continue_launchesExactlyOnce() {
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.Launched)
        composeRule.setContent { PaymentHandoffDialog(payment, launcher, onDismiss = {}, onHandoffCompleted = {}) }
        composeRule.onNodeWithText("Continue").performClick()
        assert(launcher.launchCount == 1)
    }

    @Test fun launched_reportsOutcome() {
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.Launched)
        var result: UpiPaymentLaunchResult? = null
        composeRule.setContent { PaymentHandoffDialog(payment, launcher, onDismiss = {}, onHandoffCompleted = { result = it }) }
        composeRule.onNodeWithText("Continue").performClick()
        assert(result == UpiPaymentLaunchResult.Launched)
    }

    @Test fun noUpiApp_reportsOutcome() {
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.NoUpiApp)
        var result: UpiPaymentLaunchResult? = null
        composeRule.setContent { PaymentHandoffDialog(payment, launcher, onDismiss = {}, onHandoffCompleted = { result = it }) }
        composeRule.onNodeWithText("Continue").performClick()
        assert(result == UpiPaymentLaunchResult.NoUpiApp)
    }
}

private class FakeCardRepository(initialCards: List<Card>) : CardRepository {
    private val cards = MutableStateFlow(initialCards)
    override fun observeCards(): Flow<List<Card>> = cards
    override suspend fun addCard(card: Card): Long { cards.value = cards.value + card; return card.id }
    override suspend fun updateCard(card: Card) { cards.value = cards.value.map { if (it.id == card.id) card else it } }
    override suspend fun deleteCard(cardId: Long) { cards.value = cards.value.filterNot { it.id == cardId } }
}

private class RecordingLauncher(private val result: UpiPaymentLaunchResult) : UpiPaymentLauncher {
    var launchCount = 0
        private set
    override fun launch(payment: UpiPaymentRequest): UpiPaymentLaunchResult { launchCount += 1; return result }
}
