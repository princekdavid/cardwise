package com.cardwise.app.ui

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cardwise.app.domain.model.PaymentHistoryEntry
import com.cardwise.app.domain.model.PaymentHistoryOutcome
import com.cardwise.app.domain.repository.PaymentHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InsightsScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun insights_withHistory_showsSummaryCategoriesAndMilestones() {
        val history = FakePaymentHistoryRepository(
            listOf(
                entry(1, "Dining", 60.0),
                entry(2, "Travel", 55.0)
            )
        )
        composeRule.setContent { CardWiseApp(paymentHistoryRepository = history) }
        composeRule.onNodeWithText("Insights").performClick()
        composeRule.onNodeWithTag("insights_total_reward").assertExists()
        composeRule.onNodeWithText("₹115.00").assertExists()
        composeRule.onNodeWithTag("insights_payment_count").assertExists()
        composeRule.onNodeWithText("DINING").assertExists()
        composeRule.onNodeWithText("TRAVEL").assertExists()
        composeRule.onNodeWithText("₹100 rewards").assertExists()
    }

    @Test
    fun insights_withoutHistory_showsEmptyState() {
        composeRule.setContent { CardWiseApp(paymentHistoryRepository = FakePaymentHistoryRepository(emptyList())) }
        composeRule.onNodeWithText("Insights").performClick()
        composeRule.onNodeWithText("Build your history").assertExists()
    }

    private fun entry(id: Long, category: String, reward: Double) = PaymentHistoryEntry(
        id = id,
        occurredAtEpochMillis = id,
        amount = 1000.0,
        category = category,
        cardId = 1L,
        rewardAmount = reward,
        outcome = PaymentHistoryOutcome.HANDOFF_STARTED
    )
}

private class FakePaymentHistoryRepository(initialEntries: List<PaymentHistoryEntry>) : PaymentHistoryRepository {
    private val entries = MutableStateFlow(initialEntries)

    override fun observeEntries(): Flow<List<PaymentHistoryEntry>> = entries

    override suspend fun record(entry: PaymentHistoryEntry) {
        entries.value = entries.value + entry
    }

    override suspend fun clear() {
        entries.value = emptyList()
    }
}
