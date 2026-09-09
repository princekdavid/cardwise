package com.cardwise.app.domain.recommendation

/** Deterministic, user-safe explanation of how a recommendation was evaluated. */
data class RecommendationTrace(
    val steps: List<RecommendationTraceStep>,
    val outcome: RecommendationTraceOutcome
)

data class RecommendationTraceStep(
    val id: String,
    val title: String,
    val detail: String,
    val status: RecommendationTraceStatus
)

enum class RecommendationTraceStatus { COMPLETED, CURRENT, PENDING }

enum class RecommendationTraceOutcome { MATCHED, NO_MATCH }
