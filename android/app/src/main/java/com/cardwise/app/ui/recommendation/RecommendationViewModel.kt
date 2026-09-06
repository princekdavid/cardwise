package com.cardwise.app.ui.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.recommendation.BenefitCatalogRuleMapper
import com.cardwise.app.domain.repository.BenefitCatalogRepository
import com.cardwise.app.domain.repository.CardRepository
import com.cardwise.app.domain.repository.RewardRuleRepository
import com.cardwise.app.domain.recommendation.PaymentContext
import com.cardwise.app.domain.recommendation.RecommendationEngine
import com.cardwise.app.domain.rewards.RewardRule
import com.cardwise.app.domain.scan.UpiPaymentRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class RecommendationViewModel(
    private val repository: CardRepository,
    private val rewardRuleRepository: RewardRuleRepository? = null,
    private val benefitCatalogRepository: BenefitCatalogRepository? = null,
    private val rules: Map<Long, List<RewardRule>> = emptyMap()
) : ViewModel() {
    private val _uiState = MutableStateFlow<RecommendationUiState>(
        RecommendationUiState.Loading(RecommendationInput())
    )
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()

    private var latestCards = emptyList<Card>()
    private var latestRules = rules
    private var observeJob: Job? = null
    private var input = RecommendationInput()

    init {
        observeData()
    }

    fun setAmount(value: String) {
        input = input.copy(amount = value)
        recompute()
    }

    fun setCategory(value: String) {
        input = input.copy(category = value)
        recompute()
    }

    /** Prefills only user-visible, non-sensitive payment fields from a scanned UPI QR. */
    fun prefillFromUpi(payment: UpiPaymentRequest) {
        require(payment.currency.equals("INR", ignoreCase = true)) { "Only INR payments are supported" }
        input = input.copy(amount = payment.amount?.toPlainString().orEmpty())
        recompute()
    }

    fun retry() {
        observeData()
    }

    private fun observeData() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.value = RecommendationUiState.Loading(input)
            try {
                val rulesFlow: Flow<Map<Long, List<RewardRule>>> =
                    rewardRuleRepository?.observeRules() ?: kotlinx.coroutines.flow.flowOf(rules)
                val catalogFlow = benefitCatalogRepository?.observeCatalog()
                    ?: kotlinx.coroutines.flow.flowOf(null)
                combine(repository.observeCards(), rulesFlow, catalogFlow) { cards, persistedRules, catalog ->
                    Triple(cards, persistedRules, catalog)
                }.collect { (cards, persistedRules, catalog) ->
                    latestCards = cards
                    latestRules = BenefitCatalogRuleMapper.merge(
                        catalogueRules = catalog?.let(BenefitCatalogRuleMapper::toRewardRules).orEmpty(),
                        explicitRules = persistedRules
                    )
                    recompute()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                _uiState.value = RecommendationUiState.Error(
                    input = input,
                    message = error.message ?: "We couldn't load your cards."
                )
            }
        }
    }

    private fun recompute() {
        val amount = input.amount.toDoubleOrNull()
        val category = input.category.trim()

        if (amount == null || amount <= 0.0 || category.isEmpty()) {
            _uiState.value = RecommendationUiState.Empty(input)
            return
        }

        runCatching {
            RecommendationEngine.recommend(
                context = PaymentContext(category = category, amount = amount),
                cards = latestCards,
                rules = latestRules
            )
        }.onSuccess { recommendations ->
            _uiState.value = if (recommendations.isEmpty()) {
                RecommendationUiState.Empty(input)
            } else {
                RecommendationUiState.Ready(input, recommendations)
            }
        }.onFailure { error ->
            _uiState.value = RecommendationUiState.Error(
                input = input,
                message = error.message ?: "We couldn't calculate a recommendation."
            )
        }
    }
}
