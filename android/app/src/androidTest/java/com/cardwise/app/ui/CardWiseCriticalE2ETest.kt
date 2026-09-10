package com.cardwise.app.ui

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.model.PaymentHistoryEntry
import com.cardwise.app.domain.repository.CardRepository
import com.cardwise.app.domain.repository.OnboardingRepository
import com.cardwise.app.domain.repository.PaymentHistoryRepository
import com.cardwise.app.domain.rewards.RewardRule
import com.cardwise.app.domain.scan.UpiPaymentRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardWiseCriticalE2ETest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun scannedPayment_successfulHandoff_recordsHistory() {
        val launcher = E2ERecordingLauncher(UpiPaymentLaunchResult.Launched)
        val history = E2EPaymentHistoryRepository()
        val card = Card(101L, "CardWise Bank", "Everyday Rewards", "1234", CardNetwork.VISA)
        val payment = UpiPaymentRequest(
            vpa = "merchant@upi",
            merchantName = "CardWise Shop",
            amount = BigDecimal("125.00"),
            currency = "INR",
            transactionReference = "ref-e2e",
            note = "Order 42",
            merchantCategory = "5812"
        )

        composeRule.setContent {
            CardWiseApp(
                repository = E2ECardRepository(listOf(card)),
                recommendationRules = mapOf(card.id to listOf(RewardRule("dining", rewardRatePercent = 5.0))),
                paymentLauncher = launcher,
                paymentHistoryRepository = history,
                initialPayment = payment,
                onboardingRepository = E2ECompletedOnboardingRepository()
            )
        }

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
        composeRule.onNodeWithTag("recommendation_content").performScrollToNode(hasTestTag("continue_to_upi"))
        composeRule.onNodeWithTag("continue_to_upi").performClick()
        composeRule.onNodeWithText("Continue to your UPI app?").assertExists()
        composeRule.onNodeWithText("Choose UPI app").performClick()

        assertEquals(1, launcher.launchCount)
        assertTrue(history.awaitRecorded())
        val entry = history.entries.value.single()
        assertEquals(125.0, entry.amount, 0.001)
        assertEquals("dining", entry.category)
        assertEquals(card.id, entry.cardId)
        assertEquals(6.25, entry.rewardAmount, 0.001)
    }
}

private class E2ECardRepository(initialCards: List<Card>) : CardRepository {
    private val cards = MutableStateFlow(initialCards)
    override fun observeCards(): Flow<List<Card>> = cards
    override suspend fun addCard(card: Card): Long { cards.value = cards.value + card; return card.id }
    override suspend fun updateCard(card: Card) { cards.value = cards.value.map { if (it.id == card.id) card else it } }
    override suspend fun deleteCard(cardId: Long) { cards.value = cards.value.filterNot { it.id == cardId } }
}

private class E2EPaymentHistoryRepository : PaymentHistoryRepository {
    val entries = MutableStateFlow<List<PaymentHistoryEntry>>(emptyList())
    private val recorded = CountDownLatch(1)
    override fun observeEntries(): Flow<List<PaymentHistoryEntry>> = entries
    override suspend fun record(entry: PaymentHistoryEntry) {
        entries.value = entries.value + entry
        recorded.countDown()
    }
    override suspend fun clear() { entries.value = emptyList() }
    fun awaitRecorded(): Boolean = recorded.await(5, TimeUnit.SECONDS)
}

private class E2ECompletedOnboardingRepository : OnboardingRepository {
    override fun isCompleted(): Boolean = true
    override fun complete() = Unit
    override fun reset() = Unit
}

private class E2ERecordingLauncher(private val result: UpiPaymentLaunchResult) : UpiPaymentLauncher {
    var launchCount = 0
        private set
    override fun launch(payment: UpiPaymentRequest): UpiPaymentLaunchResult {
        launchCount += 1
        return result
    }
}
