package com.cardwise.app.domain.recommendation

import com.cardwise.app.domain.model.BenefitCatalogSnapshot
import com.cardwise.app.domain.rewards.RewardRule

/** Converts canonical catalogue benefits into the existing reward-rule model. */
object BenefitCatalogRuleMapper {
    fun toRewardRules(snapshot: BenefitCatalogSnapshot): Map<Long, List<RewardRule>> =
        snapshot.entries
            .asSequence()
            .filter { it.rewardRatePercent != null }
            .flatMap { entry ->
                entry.categories.asSequence().map { category ->
                    entry.cardId to (category to RewardRule(
                        category = category,
                        rewardRatePercent = entry.rewardRatePercent ?: 0.0,
                        maxRewardAmount = entry.maxRewardAmount,
                        minimumSpend = entry.minimumSpend,
                        maximumEligibleSpend = entry.maximumEligibleSpend
                    ))
                }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, rules) ->
                rules.groupBy { it.first.trim().lowercase() }
                    .values
                    .map { categoryRules ->
                        categoryRules.maxWithOrNull(
                            compareBy<Pair<String, RewardRule>> { it.second.rewardRatePercent }
                                .thenBy { it.second.maximumEligibleSpend ?: Double.POSITIVE_INFINITY }
                                .thenBy { it.second.maxRewardAmount ?: Double.POSITIVE_INFINITY }
                        )!!.second
                    }
            }

    /** Catalogue rules supplement explicit user rules; explicit rules always win by category. */
    fun merge(
        catalogueRules: Map<Long, List<RewardRule>>,
        explicitRules: Map<Long, List<RewardRule>>
    ): Map<Long, List<RewardRule>> {
        val cardIds = catalogueRules.keys + explicitRules.keys
        return cardIds.associateWith { cardId ->
            val explicit = explicitRules[cardId].orEmpty()
            val explicitCategories = explicit.map { it.category.trim().lowercase() }.toSet()
            catalogueRules[cardId].orEmpty().filterNot {
                it.category.trim().lowercase() in explicitCategories
            } + explicit
        }
    }
}
