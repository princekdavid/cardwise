package com.cardwise.app.domain.insights

import com.cardwise.app.domain.model.PaymentHistoryEntry
import java.math.BigDecimal

/** Deterministic, explainable aggregation of locally stored payment history. */
class InsightsEngine {
    fun summarize(entries: List<PaymentHistoryEntry>): InsightsSummary {
        val ordered = entries.sortedByDescending { it.occurredAtEpochMillis }
        val totalReward = ordered.fold(BigDecimal.ZERO) { total, entry ->
            total + entry.rewardAmount.toBigDecimal()
        }
        val categoryTotals = ordered
            .groupBy { it.category.trim().ifBlank { "Other" }.uppercase() }
            .mapValues { (_, values) -> values.fold(BigDecimal.ZERO) { total, entry -> total + entry.rewardAmount.toBigDecimal() } }
            .toList()
            .sortedWith(compareByDescending<Pair<String, BigDecimal>> { it.second }.thenBy { it.first })
        val milestones = listOf(
            Milestone("first_payment", "First payment", ordered.size >= 1, minOf(ordered.size, 1), 1),
            Milestone("ten_payments", "10 optimized payments", ordered.size >= 10, minOf(ordered.size, 10), 10),
            Milestone("hundred_reward", "₹100 rewards", totalReward >= BigDecimal("100"), minOf(totalReward.toInt(), 100), 100)
        )
        return InsightsSummary(
            paymentCount = ordered.size,
            totalReward = totalReward,
            categoryRewardTotals = categoryTotals,
            milestones = milestones,
            hasEnoughHistory = ordered.isNotEmpty()
        )
    }
}

data class InsightsSummary(
    val paymentCount: Int,
    val totalReward: BigDecimal,
    val categoryRewardTotals: List<Pair<String, BigDecimal>>,
    val milestones: List<Milestone>,
    val hasEnoughHistory: Boolean
)

data class Milestone(
    val id: String,
    val title: String,
    val completed: Boolean,
    val progress: Int,
    val target: Int
)
