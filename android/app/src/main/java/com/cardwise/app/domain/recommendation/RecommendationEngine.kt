package com.cardwise.app.domain.recommendation

import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.rewards.RewardCalculator
import com.cardwise.app.domain.rewards.RewardRule

/** Pure, deterministic ranking of cards for a payment context. */
object RecommendationEngine {
    fun recommend(
        context: PaymentContext,
        cards: List<Card>,
        rules: Map<Long, List<RewardRule>>
    ): List<CardRecommendation> {
        val candidates = cards.asSequence()
            .filter { it.isActive }
            .mapNotNull { card ->
                val rule = rules[card.id]
                    ?.firstOrNull { it.enabled && it.category.equals(context.category, ignoreCase = true) }
                    ?: return@mapNotNull null
                val estimate = RewardCalculator.estimate(context.amount, rule)
                if (estimate.estimatedReward <= 0.0 && context.amount > 0.0) return@mapNotNull null
                CardRecommendation(
                    card = card,
                    reward = estimate,
                    reason = reasonFor(context, estimate, rule),
                    rank = 0
                )
            }
            .sortedWith(
                compareByDescending<CardRecommendation> { it.reward.estimatedReward }
                    .thenByDescending { it.reward.eligibleSpend }
                    .thenBy { it.card.id }
            )
            .toList()

        return candidates.mapIndexed { index, recommendation ->
            recommendation.copy(rank = index + 1)
        }
    }

    private fun reasonFor(
        context: PaymentContext,
        estimate: com.cardwise.app.domain.rewards.RewardEstimate,
        rule: RewardRule
    ): String = buildString {
        append("Earn approximately ")
        append(estimate.estimatedReward.formatCurrency())
        append(" on ")
        append(context.category)
        append(".")
        if (rule.rewardRatePercent > 0) {
            append(" ${rule.rewardRatePercent.formatRate()}% rewards")
        }
        if (estimate.capped) append(" (cap applied)")
    }

    private fun Double.formatCurrency(): String = String.format("%.2f", this)
    private fun Double.formatRate(): String = String.format("%.2f", this)
}
