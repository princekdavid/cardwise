package com.cardwise.app.domain.recommendation

import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.rewards.RewardCalculator
import com.cardwise.app.domain.rewards.RewardRule
import java.util.Locale

/** Pure, deterministic ranking of cards for a payment context. */
object RecommendationEngine {
    fun recommend(
        context: PaymentContext,
        cards: List<Card>,
        rules: Map<Long, List<RewardRule>>
    ): List<CardRecommendation> = evaluate(context, cards, rules).recommendations

    fun evaluate(
        context: PaymentContext,
        cards: List<Card>,
        rules: Map<Long, List<RewardRule>>
    ): RecommendationEvaluation {
        val normalizedCategory = context.category.trim()
        val trace = mutableListOf<RecommendationTraceStep>()
        trace += RecommendationTraceStep(
            "context", "Normalize payment context",
            "Using ${normalizedCategory.ifBlank { "an unspecified category" }} for ₹${String.format(Locale.ROOT, "%.2f", context.amount)}.",
            RecommendationTraceStatus.COMPLETED
        )

        if (context.amount <= 0.0 || normalizedCategory.isEmpty()) {
            trace += RecommendationTraceStep("eligibility", "Check eligible cards", "A positive amount and category are required.", RecommendationTraceStatus.COMPLETED)
            trace += RecommendationTraceStep("benefit", "Calculate benefits", "No benefit calculation was possible.", RecommendationTraceStatus.PENDING)
            trace += RecommendationTraceStep("ranking", "Rank outcomes", "No recommendation was produced.", RecommendationTraceStatus.PENDING)
            return RecommendationEvaluation(emptyList(), RecommendationTrace(trace, RecommendationTraceOutcome.NO_MATCH))
        }

        val activeCards = cards.asSequence().filter { it.isActive }.distinctBy { it.id }.toList()
        val candidates = activeCards.mapNotNull { card ->
            val matchingRules = rules[card.id].orEmpty().filter {
                it.enabled && it.category.trim().equals(normalizedCategory, ignoreCase = true)
            }
            if (matchingRules.isEmpty()) return@mapNotNull null

            val bestRule = matchingRules.maxWithOrNull(
                compareBy<RewardRule> { RewardCalculator.estimate(context.amount, it).estimatedReward }
                    .thenBy { RewardCalculator.estimate(context.amount, it).eligibleSpend }
                    .thenBy { it.rewardRatePercent }
                    .thenBy { it.maximumEligibleSpend ?: Double.POSITIVE_INFINITY }
                    .thenBy { it.maxRewardAmount ?: Double.POSITIVE_INFINITY }
            ) ?: return@mapNotNull null
            val estimate = RewardCalculator.estimate(context.amount, bestRule)
            if (estimate.estimatedReward <= 0.0) return@mapNotNull null
            Triple(card, estimate, bestRule)
        }

        trace += RecommendationTraceStep(
            "eligibility", "Check eligible cards",
            "${candidates.size} active card${if (candidates.size == 1) "" else "s"} matched an enabled ${normalizedCategory} reward rule.",
            RecommendationTraceStatus.COMPLETED
        )

        val recommendations = candidates.sortedWith(
            compareByDescending<Triple<Card, com.cardwise.app.domain.rewards.RewardEstimate, RewardRule>> { it.second.estimatedReward }
                .thenByDescending { it.second.eligibleSpend }
                .thenBy { it.first.id }
        ).mapIndexed { index, (card, estimate, rule) ->
            CardRecommendation(card, estimate, reasonFor(context, estimate, rule), index + 1)
        }

        trace += RecommendationTraceStep(
            "benefit", "Calculate benefits",
            if (recommendations.isEmpty()) "No positive benefit was available." else "Calculated reward estimates from the matched local rules.",
            RecommendationTraceStatus.COMPLETED
        )
        trace += RecommendationTraceStep(
            "ranking", "Rank outcomes",
            if (recommendations.isEmpty()) "There is no eligible recommendation." else "Ranked by estimated reward, then eligible spend, then stable card ID.",
            RecommendationTraceStatus.COMPLETED
        )

        return RecommendationEvaluation(
            recommendations,
            RecommendationTrace(trace, if (recommendations.isEmpty()) RecommendationTraceOutcome.NO_MATCH else RecommendationTraceOutcome.MATCHED)
        )
    }

    data class RecommendationEvaluation(
        val recommendations: List<CardRecommendation>,
        val trace: RecommendationTrace
    )

    private fun reasonFor(
        context: PaymentContext,
        estimate: com.cardwise.app.domain.rewards.RewardEstimate,
        rule: RewardRule
    ): String = buildString {
        append("Earn approximately ")
        append(estimate.estimatedReward.formatCurrency())
        append(" on ")
        append(context.category.trim())
        append(".")
        if (rule.rewardRatePercent > 0) append(" ${rule.rewardRatePercent.formatRate()}% rewards")
        if (estimate.capped) append(" (cap applied)")
    }

    private fun Double.formatCurrency(): String = String.format(Locale.ROOT, "%.2f", this)
    private fun Double.formatRate(): String = String.format(Locale.ROOT, "%.2f", this)
}
