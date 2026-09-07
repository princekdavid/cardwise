package com.cardwise.app.ui.recommendation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.repository.CardRepository
import com.cardwise.app.domain.rewards.RewardRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecommendationScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun enteringPurchase_showsTopRecommendation() {
        val card = Card(1L, "HDFC", "Dining Card", "4321", CardNetwork.VISA)
        val repository = FakeRecommendationRepository(card)
        val viewModel = RecommendationViewModel(
            repository = repository,
            rules = mapOf(1L to listOf(RewardRule("Dining", 5.0)))
        )

        composeRule.setContent {
            RecommendationScreen(viewModel = viewModel)
        }

        composeRule.onNodeWithText("Amount").performTextInput("1000")
        composeRule.onNodeWithText("Category").performTextInput("Dining")

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Recommended for this payment").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Recommended for this payment").assertExists()
        composeRule.onNodeWithText("Dining Card").assertExists()
        composeRule.onNodeWithText("₹50.00").assertExists()
    }
}

private class FakeRecommendationRepository(vararg initialCards: Card) : CardRepository {
    private val cards = MutableStateFlow(initialCards.toList())

    override fun observeCards(): Flow<List<Card>> = cards
    override suspend fun addCard(card: Card): Long = error("Not needed")
    override suspend fun updateCard(card: Card) = error("Not needed")
    override suspend fun deleteCard(cardId: Long) = error("Not needed")
}
