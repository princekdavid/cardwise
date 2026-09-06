package com.cardwise.app.ui.recommendation

import com.cardwise.app.domain.model.BenefitCatalogEntry
import com.cardwise.app.domain.model.BenefitCatalogSnapshot
import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.repository.BenefitCatalogRepository
import com.cardwise.app.domain.repository.CardRepository
import com.cardwise.app.domain.repository.RewardRuleRepository
import com.cardwise.app.domain.rewards.RewardRule
import com.cardwise.app.domain.scan.UpiPaymentRequest
import com.cardwise.app.ui.wallet.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
    fun catalogBenefits_driveRecommendationsWithoutUserRules() = runTest {
        val repository = FakeRecommendationRepository(card(1L))
        val catalogRepository = FakeBenefitCatalogRepository(
            BenefitCatalogSnapshot(
                version = 7L,
                entries = listOf(
                    BenefitCatalogEntry(
                        cardId = 1L,
                        benefitId = "dining-5",
                        title = "Dining rewards",
                        description = "Earn 5 percent on dining.",
                        categories = setOf("Dining"),
                        rewardRatePercent = 5.0
                    )
                )
            )
        )
        val viewModel = RecommendationViewModel(
            repository = repository,
            benefitCatalogRepository = catalogRepository
        )

        viewModel.setAmount("1000")
        viewModel.setCategory("dining")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertIs<RecommendationUiState.Ready>(state)
        assertEquals(50.0, state.recommendations.single().reward.estimatedReward)
    }

    @Test
    fun explicitUserRule_overridesCatalogRuleForSameCategory() = runTest {
        val repository = FakeRecommendationRepository(card(1L))
        val ruleRepository = FakeRewardRuleRepository(
            mapOf(1L to listOf(RewardRule("Dining", 2.0)))
        )
        val catalogRepository = FakeBenefitCatalogRepository(
            BenefitCatalogSnapshot(
                version = 8L,
                entries = listOf(
                    BenefitCatalogEntry(1L, "dining-5", "Dining", "Catalog", setOf("Dining"), rewardRatePercent = 5.0)
                )
            )
        )
        val viewModel = RecommendationViewModel(
            repository = repository,
            rewardRuleRepository = ruleRepository,
            benefitCatalogRepository = catalogRepository
        )

        viewModel.setAmount("1000")
        viewModel.setCategory("Dining")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertIs<RecommendationUiState.Ready>(state)
        assertEquals(20.0, state.recommendations.single().reward.estimatedReward)
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

    @Test
    fun scannedUpiPayment_prefillsAmount_butLeavesCategoryForUser() = runTest {
        val repository = FakeRecommendationRepository(card(1L))
        val viewModel = RecommendationViewModel(repository)

        viewModel.prefillFromUpi(
            SCANNED_UPI_PAYMENT.copy(amount = BigDecimal("125.50"))
        )
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertIs<RecommendationUiState.Empty>(state)
        assertEquals("125.50", state.input.amount)
        assertEquals("", state.input.category)
    }

    @Test
    fun scannedUpiPayment_withoutAmount_keepsAmountEmpty() = runTest {
        val repository = FakeRecommendationRepository(card(1L))
        val viewModel = RecommendationViewModel(repository)

        viewModel.prefillFromUpi(SCANNED_UPI_PAYMENT.copy(amount = null))
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertIs<RecommendationUiState.Empty>(state)
        assertEquals("", state.input.amount)
    }

    @Test
    fun scannedUpiPayment_withUnsupportedCurrency_isRejected() = runTest {
        val viewModel = RecommendationViewModel(FakeRecommendationRepository(card(1L)))

        assertFailsWith<IllegalArgumentException> {
            viewModel.prefillFromUpi(SCANNED_UPI_PAYMENT.copy(currency = "USD"))
        }
    }

    private fun card(id: Long) = Card(
        id = id,
        issuer = "Bank",
        name = "Rewards Card $id",
        lastFour = "1234",
        network = CardNetwork.VISA
    )
}

private val SCANNED_UPI_PAYMENT = UpiPaymentRequest(
    vpa = "merchant@upi",
    merchantName = "Shop",
    amount = BigDecimal("100.00"),
    currency = "INR",
    transactionReference = "TX1",
    note = "Purchase"
)

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

private class FakeBenefitCatalogRepository(
    initialSnapshot: BenefitCatalogSnapshot
) : BenefitCatalogRepository {
    private val snapshot = MutableStateFlow(initialSnapshot)

    override fun observeCatalog(): Flow<BenefitCatalogSnapshot> = snapshot

    override suspend fun replaceCatalog(snapshot: BenefitCatalogSnapshot) {
        this.snapshot.value = snapshot
    }
}
