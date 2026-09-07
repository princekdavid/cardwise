package com.cardwise.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cardwise.app.domain.model.BenefitCatalogEntry
import com.cardwise.app.domain.model.BenefitCatalogSnapshot
import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.repository.BenefitCatalogRepository
import com.cardwise.app.domain.repository.CardRepository
import com.cardwise.app.domain.repository.RewardRuleRepository
import com.cardwise.app.domain.rewards.RewardRule
import com.cardwise.app.domain.scan.UpiPaymentRequest
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CardWiseAppPaymentFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun scannedPaymentIsDisplayedAndRecommendationIsRenderedWithoutRescan() {
        val payment = testPayment()
        val card = testCard()
        val repository = FakeCardRepository(listOf(card))
        val rules = FakeRewardRuleRepository(
            mapOf(card.id to listOf(RewardRule(category = "Groceries", rewardRatePercent = 5.0)))
        )
        val catalog = FakeBenefitCatalogRepository(
            BenefitCatalogSnapshot(
                version = 1L,
                entries = listOf(
                    BenefitCatalogEntry(
                        cardId = card.id,
                        benefitId = "fresh-mart",
                        title = "Fresh Mart rewards",
                        description = "Extra rewards at Fresh Mart",
                        categories = setOf("Groceries"),
                        merchantHints = setOf("fresh mart")
                    )
                )
            )
        )

        composeRule.setContent {
            CardWiseApp(
                repository = repository,
                rewardRuleRepository = rules,
                benefitCatalogRepository = catalog,
                initialPayment = payment
            )
        }

        composeRule.onNodeWithText("Scanned payment").assertIsDisplayed()
        composeRule.onNodeWithText("Fresh Mart").assertIsDisplayed()
        composeRule.onNodeWithText("₹1250.50").assertIsDisplayed()
        composeRule.onNodeWithText("Recommended for this payment").assertIsDisplayed()
        composeRule.onNodeWithText("₹62.50").assertIsDisplayed()
        composeRule.onNodeWithText("Continue to UPI app").assertIsDisplayed()
    }

    @Test
    fun continueToUpiAppUsesTheSameScannedPayment() {
        val payment = testPayment()
        val card = testCard()
        val launcher = FakePaymentLauncher()

        composeRule.setContent {
            CardWiseApp(
                repository = FakeCardRepository(listOf(card)),
                rewardRuleRepository = FakeRewardRuleRepository(
                    mapOf(card.id to listOf(RewardRule(category = "Groceries", rewardRatePercent = 5.0)))
                ),
                benefitCatalogRepository = FakeBenefitCatalogRepository(
                    BenefitCatalogSnapshot(
                        version = 1L,
                        entries = listOf(
                            BenefitCatalogEntry(
                                cardId = card.id,
                                benefitId = "fresh-mart",
                                title = "Fresh Mart rewards",
                                description = "Extra rewards at Fresh Mart",
                                categories = setOf("Groceries"),
                                merchantHints = setOf("fresh mart")
                            )
                        )
                    )
                ),
                paymentLauncher = launcher,
                initialPayment = payment
            )
        }

        composeRule.onNodeWithText("Continue to UPI app").performClick()
        composeRule.onNodeWithText("Continue to your UPI app?").assertIsDisplayed()
        composeRule.onNodeWithText("Continue", useUnmergedTree = true).performClick()

        composeRule.runOnIdle {
            assertEquals(payment, launcher.lastPayment)
        }
    }

    private fun testPayment() = UpiPaymentRequest(
        vpa = "merchant@upi",
        merchantName = "Fresh Mart",
        amount = BigDecimal("1250.50"),
        currency = "INR",
        transactionReference = "order-42",
        note = "Weekly groceries"
    )

    private fun testCard() = Card(
        id = 7L,
        issuer = "Test Bank",
        name = "Rewards Card",
        lastFour = "1234",
        network = CardNetwork.VISA
    )

    private class FakeCardRepository(cards: List<Card>) : CardRepository {
        private val state = MutableStateFlow(cards)
        override fun observeCards(): Flow<List<Card>> = state
        override suspend fun addCard(card: Card): Long = card.id
        override suspend fun updateCard(card: Card) {
            state.value = state.value.map { if (it.id == card.id) card else it }
        }
        override suspend fun deleteCard(cardId: Long) {
            state.value = state.value.filterNot { it.id == cardId }
        }
    }

    private class FakeRewardRuleRepository(initial: Map<Long, List<RewardRule>>) : RewardRuleRepository {
        private val state = MutableStateFlow(initial)
        override fun observeRules(): Flow<Map<Long, List<RewardRule>>> = state
        override suspend fun getRules(cardId: Long): List<RewardRule> = state.value[cardId].orEmpty()
        override suspend fun replaceRules(cardId: Long, rules: List<RewardRule>) {
            state.value = state.value + (cardId to rules)
        }
    }

    private class FakeBenefitCatalogRepository(snapshot: BenefitCatalogSnapshot) : BenefitCatalogRepository {
        private val state = MutableStateFlow(snapshot)
        override fun observeCatalog(): Flow<BenefitCatalogSnapshot> = state
        override suspend fun replaceCatalog(snapshot: BenefitCatalogSnapshot) {
            state.value = snapshot
        }
    }

    private class FakePaymentLauncher : UpiPaymentLauncher {
        var lastPayment: UpiPaymentRequest? = null
            private set

        override fun launch(payment: UpiPaymentRequest): UpiPaymentLaunchResult {
            lastPayment = payment
            return UpiPaymentLaunchResult.Launched
        }
    }
}
