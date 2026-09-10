package com.cardwise.app.ui

import android.graphics.Bitmap
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.repository.CardRepository
import com.cardwise.app.domain.repository.OnboardingRepository
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
    @get:Rule val composeRule = createComposeRule()

    @Test fun cockpit_isSelectedInitially() { composeRule.setContent { CardWiseApp(onboardingRepository = CompletedOnboardingRepository()) }; composeRule.onNodeWithText("Cockpit").assertIsSelected() }

    @Test fun onboarding_requiresExplicitPrivacyAcceptance() {
        val onboarding = FakeOnboardingRepository(completed = false)
        composeRule.setContent { CardWiseApp(onboardingRepository = onboarding) }
        composeRule.onNodeWithText("Privacy oath").assertExists()
        composeRule.onNodeWithText("I understand — continue").performClick()
        composeRule.onNodeWithText("Cockpit").assertIsSelected()
        assert(onboarding.isCompleted())
    }

    @Test fun selectingCards_updatesSelectedDestination() {
        composeRule.setContent { CardWiseApp(onboardingRepository = CompletedOnboardingRepository()) }
        composeRule.onNodeWithTag("nav_wallet").performClick()
        composeRule.onNodeWithTag("nav_wallet").assertIsSelected()
        composeRule.onNodeWithText("My Physical Deck").assertExists()
    }

    @Test fun emptyDeck_showsCatalogueEntryPoint() {
        composeRule.setContent { CardWiseApp(repository = FakeCardRepository(emptyList()), onboardingRepository = CompletedOnboardingRepository()) }
        composeRule.onNodeWithTag("nav_wallet").performClick()
        composeRule.onNodeWithText("No cards in your deck").assertExists()
        composeRule.onNodeWithText("Browse card catalogue").assertExists()
    }

    @Test fun cardCatalogue_exposesSearchAndFilterControls() {
        composeRule.setContent { CardWiseApp(repository = FakeCardRepository(emptyList()), onboardingRepository = CompletedOnboardingRepository()) }
        composeRule.onNodeWithTag("nav_wallet").performClick()
        composeRule.onNodeWithText("Browse card catalogue").performClick()
        composeRule.onNodeWithTag("catalog_search").assertExists()
        composeRule.onNodeWithTag("catalog_filters").assertExists()
        composeRule.onNodeWithText("Travel").performClick()
        composeRule.onNodeWithText("Travel").assertIsSelected()
    }

    @Test fun populatedDeck_showsCardAndDetailsEntryPoint() {
        val card = Card(7L, "CardWise Bank", "Everyday Rewards", "1234", CardNetwork.VISA)
        composeRule.setContent { CardWiseApp(repository = FakeCardRepository(listOf(card)), onboardingRepository = CompletedOnboardingRepository()) }
        composeRule.onNodeWithTag("nav_wallet").performClick()
        composeRule.onNodeWithTag("wallet_tactile_deck").assertExists()
        composeRule.onNodeWithTag("wallet_spotlight_card_7").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeRule.onNodeWithTag("card_detail_name").assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeRule.onNodeWithTag("card_detail_name").assertExists()
    }

    @Test fun tactileDeck_exposesSpotlightCard() {
        val card = Card(7L, "CardWise Bank", "Everyday Rewards", "1234", CardNetwork.VISA)
        composeRule.setContent { CardWiseApp(repository = FakeCardRepository(listOf(card)), onboardingRepository = CompletedOnboardingRepository()) }
        composeRule.onNodeWithTag("nav_wallet").performClick()
        composeRule.onNodeWithTag("wallet_tactile_deck").assertExists()
        composeRule.onNodeWithTag("wallet_spotlight_card_7").assertExists()
    }

    @Test fun activeFilter_hidesInactiveCards() {
        val active = Card(1L, "CardWise Bank", "Active Card", "1111", CardNetwork.VISA, isActive = true)
        val inactive = Card(2L, "CardWise Bank", "Paused Card", "2222", CardNetwork.MASTERCARD, isActive = false)
        composeRule.setContent { CardWiseApp(repository = FakeCardRepository(listOf(active, inactive)), onboardingRepository = CompletedOnboardingRepository()) }
        composeRule.onNodeWithTag("nav_wallet").performClick()
        composeRule.onNodeWithText("Active").performClick()
        composeRule.onNodeWithTag("wallet_card_1").assertExists()
        composeRule.onAllNodesWithTag("wallet_card_2").assertCountEquals(0)
    }

    @Test fun selectingOffers_showsOfferSurface() {
        composeRule.setContent { CardWiseApp(onboardingRepository = CompletedOnboardingRepository()) }
        composeRule.onNodeWithText("Offers").performClick()
        composeRule.onNodeWithText("Offers").assertIsSelected()
        composeRule.onNodeWithText("Active Card Promos").assertExists()
    }

    @Test fun selectingScan_withoutPermission_showsPrivacyFirstCameraPrompt() {
        composeRule.setContent { CardWiseApp(onboardingRepository = CompletedOnboardingRepository()) }
        composeRule.onNodeWithText("Scan").performClick()
        composeRule.onNodeWithText("Camera access needed").assertExists()
        composeRule.onNodeWithText("The QR payload is processed locally and is not stored.", substring = true).assertExists()
    }

    @Test fun scannedPayment_flowsThroughRecommendationToHandoff() {
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.Launched)
        val card = Card(1L, "CardWise Bank", "Everyday Rewards", "1234", CardNetwork.VISA)
        val payment = UpiPaymentRequest("merchant@upi", "CardWise Shop", BigDecimal("125.00"), "INR", "ref-123", "Order 42", merchantCategory = "5812")
        composeRule.setContent { CardWiseApp(repository = FakeCardRepository(listOf(card)), recommendationRules = mapOf(card.id to listOf(RewardRule("dining", rewardRatePercent = 5.0))), paymentLauncher = launcher, initialPayment = payment, onboardingRepository = CompletedOnboardingRepository()) }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            try {
                composeRule.onNodeWithTag("recommendation_winner").assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeRule.onNodeWithText("CardWise Shop").assertExists()
        composeRule.onNodeWithText("₹125.00").assertExists()
        composeRule.onNodeWithTag("recommendation_winner").assertExists()
        captureEvidence("01-recommendation")
        composeRule.onNodeWithTag("recommendation_content").performScrollToNode(hasTestTag("continue_to_upi"))
        composeRule.onNodeWithTag("continue_to_upi").assertExists().performClick()
        composeRule.onNodeWithText("Continue to your UPI app?").assertExists()
        captureEvidence("02-payment-handoff")
        composeRule.onNodeWithText("Choose UPI app").performClick()
        assert(launcher.launchCount == 1)
        composeRule.waitForIdle()
        captureEvidence("03-after-handoff-return")
    }

    @Test fun privacyVault_resetReturnsToOnboarding() {
        val onboarding = CompletedOnboardingRepository()
        val vault = FakePrivacyVaultRepository(onboarding)
        composeRule.setContent { CardWiseApp(onboardingRepository = onboarding, privacyVaultRepository = vault) }
        composeRule.onNodeWithText("Privacy").performClick()
        composeRule.onNodeWithText("Reset all data").performClick()
        composeRule.onNodeWithText("Reset Privacy Vault?").assertExists()
        composeRule.onNodeWithText("Reset").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { !onboarding.isCompleted() }
        composeRule.onNodeWithText("Privacy oath").assertExists()
        assert(vault.resetCount == 1)
    }

    private fun captureEvidence(name: String) {
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        val directory = File(
            InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null),
            "cardwise-evidence"
        ).apply { mkdirs() }
        FileOutputStream(File(directory, "$name.png")).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        bitmap.recycle()
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

    @Test fun confirmation_showsPaymentSummary() {
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.Launched)
        composeRule.setContent { PaymentHandoffDialog(payment, launcher, onDismiss = {}, onHandoffCompleted = {}) }
        composeRule.onNodeWithText("CardWise Shop").assertExists()
        composeRule.onNodeWithText("merchant@upi").assertExists()
        composeRule.onNodeWithText("₹125.00").assertExists()
        composeRule.onNodeWithText("No PIN or banking credentials are shared by CardWise.").assertExists()
    }

    @Test fun continue_launchesExactlyOnce() {
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.Launched)
        composeRule.setContent { PaymentHandoffDialog(payment, launcher, onDismiss = {}, onHandoffCompleted = {}) }
        composeRule.onNodeWithText("Choose UPI app").performClick()
        assert(launcher.launchCount == 1)
    }

    @Test fun launched_reportsOutcome() {
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.Launched)
        var result: UpiPaymentLaunchResult? = null
        composeRule.setContent { PaymentHandoffDialog(payment, launcher, onDismiss = {}, onHandoffCompleted = { result = it }) }
        composeRule.onNodeWithText("Choose UPI app").performClick()
        assert(result == UpiPaymentLaunchResult.Launched)
    }

    @Test fun noUpiApp_reportsOutcome() {
        val launcher = RecordingLauncher(UpiPaymentLaunchResult.NoUpiApp)
        var result: UpiPaymentLaunchResult? = null
        composeRule.setContent { PaymentHandoffDialog(payment, launcher, onDismiss = {}, onHandoffCompleted = { result = it }) }
        composeRule.onNodeWithText("Choose UPI app").performClick()
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

private class CompletedOnboardingRepository : OnboardingRepository {
    private var completed = true
    override fun isCompleted(): Boolean = completed
    override fun complete() { completed = true }
    override fun reset() { completed = false }
}

private class FakeOnboardingRepository(private var completed: Boolean) : OnboardingRepository {
    override fun isCompleted(): Boolean = completed
    override fun complete() { completed = true }
    override fun reset() { completed = false }
}

private class FakePrivacyVaultRepository(private val onboardingRepository: OnboardingRepository) : com.cardwise.app.domain.repository.PrivacyVaultRepository {
    var resetCount = 0
    override suspend fun resetAllData() {
        resetCount += 1
        onboardingRepository.reset()
    }
}

private class RecordingLauncher(private val result: UpiPaymentLaunchResult) : UpiPaymentLauncher {
    var launchCount = 0
        private set
    override fun launch(payment: UpiPaymentRequest): UpiPaymentLaunchResult { launchCount += 1; return result }
}