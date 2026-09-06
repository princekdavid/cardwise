package com.cardwise.app.ui.recommendation

import com.cardwise.app.domain.recommendation.CardRecommendation

data class RecommendationInput(
    val amount: String = "",
    val category: String = ""
)

sealed interface RecommendationUiState {
    data class Loading(
        val input: RecommendationInput
    ) : RecommendationUiState

    data class Ready(
        val input: RecommendationInput,
        val recommendations: List<CardRecommendation>
    ) : RecommendationUiState

    data class Empty(
        val input: RecommendationInput
    ) : RecommendationUiState

    data class Error(
        val input: RecommendationInput,
        val message: String
    ) : RecommendationUiState
}
