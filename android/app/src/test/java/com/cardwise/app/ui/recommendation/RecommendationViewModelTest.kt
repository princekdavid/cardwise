package com.cardwise.app.ui.recommendation

import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.repository.CardRepository
import com.cardwise.app.domain.repository.RewardRuleRepository
import com.cardwise.app.domain.rewards.RewardRule
import com.cardwise.app.ui.wallet.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class RecommendationViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun matchingCards_areRankedAndExposedAsReady() = runTest {
        val repository = FakeRecommendationRepository(card(1L), card(2L))
        val ruleRepository = FakeRewardRuleRepository(
            mapOf(
                1L to listOf(RewardRule("Dining", 5.0)),
                2L to listOf(RewardRule("Dining", 2.0))
            )
        )
        val viewModel = RecommendationViewModel(repository, rewardRuleRepository = ruleRepository)

        viewModel.setAmount("1000")
        viewModel.setCategory(" dining ")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertIs<RecommendationUiState.Ready>(state)
        assertEquals(1L, state.recommendations.first().card.id)
        assertEquals(50.0, state.recommendations.first().reward.estimatedReward)
    }

    @Test
    fun missingRules_produceNoEligibleCardState() = runTest {
        val repository = FakeRecommendationRepository(card(1L))
        val ruleRepository = FakeRewardRuleRepository()
        val viewModel = RecommendationViewModel(repository, rewardRuleRepository = ruleRepository)

        viewModel.setAmount("500")
        viewModel.setCategory("Travel")
        advanceUntilIdle()

        assertIs<RecommendationUiState.Empty>(viewModel.uiState.first())
    }

    @Test
    fun incompleteInput_doesNotCalculate() = runTest {
        val repository = FakeRecommendationRepository(card(1L))
        val ruleRepository = FakeRewardRuleRepository(
            mapOf(1L to listOf(RewardRule("Dining", 5.0)))
        )
        val viewModel = RecommendationViewModel(repository, rewardRuleRepository = ruleRepository)

        viewModel.setAmount("100")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertIs<RecommendationUiState.Empty>(state)
        assertEquals("100", state.input.amount)
        assertEquals("", state.input.category)
    }

    private fun card(id: Long) = Card(
        id = id,
        issuer = "Bank",
        name = "Rewards Card $id",
        lastFour = "1234",
        network = CardNetwork.VISA
    )
}

private class FakeRecommendationRepository(vararg initialCards: Card) : CardRepository {
    private val cards = MutableStateFlow(initialCards.toList())

    override fun observeCards(): Flow<List<Card>> = cards

    override suspend fun addCard(card: Card): Long = error("Not needed")
    override suspend fun updateCard(card: Card) = error("Not needed")
    override suspend fun deleteCard(cardId: Long) = error("Not needed")
}

private class FakeRewardRuleRepository(
    initialRules: Map<Long, List<RewardRule>> = emptyMap()
) : RewardRuleRepository {
    private val rules = MutableStateFlow(initialRules)

    override fun observeRules(): Flow<Map<Long, List<RewardRule>>> = rules

    override suspend fun getRules(cardId: Long): List<RewardRule> = rules.value[cardId].orEmpty()

    override suspend fun replaceRules(cardId: Long, rules: List<RewardRule>) {
        this.rules.value = this.rules.value.toMutableMap().apply {
            if (rules.isEmpty()) remove(cardId) else put(cardId, rules)
        }
    }
}
