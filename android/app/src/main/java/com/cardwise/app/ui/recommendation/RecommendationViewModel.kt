package com.cardwise.app.ui.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.recommendation.PaymentContext
import com.cardwise.app.domain.recommendation.RecommendationEngine
import com.cardwise.app.domain.repository.CardRepository
import com.cardwise.app.domain.rewards.RewardRule
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecommendationViewModel(
    private val repository: CardRepository,
    private val rules: Map<Long, List<RewardRule>> = emptyMap()
) : ViewModel() {
    private val _uiState = MutableStateFlow<RecommendationUiState>(
        RecommendationUiState.Loading(RecommendationInput())
    )
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()

    private var latestCards = emptyList<Card>()
    private var observeJob: Job? = null
    private var input = RecommendationInput()

    init {
        observeCards()
    }

    fun setAmount(value: String) {
        input = input.copy(amount = value)
        recompute()
    }

    fun setCategory(value: String) {
        input = input.copy(category = value)
        recompute()
    }

    fun retry() {
        observeCards()
    }

    private fun observeCards() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.value = RecommendationUiState.Loading(input)
            try {
                repository.observeCards().collect { cards ->
                    latestCards = cards
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
                rules = rules
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
