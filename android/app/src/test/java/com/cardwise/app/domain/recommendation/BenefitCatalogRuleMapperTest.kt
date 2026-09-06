package com.cardwise.app.domain.recommendation

import com.cardwise.app.domain.model.BenefitCatalogEntry
import com.cardwise.app.domain.model.BenefitCatalogSnapshot
import com.cardwise.app.domain.rewards.RewardRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BenefitCatalogRuleMapperTest {
    @Test
    fun mapsCatalogBenefitsIntoRewardRulesByCategory() {
        val snapshot = BenefitCatalogSnapshot(
            version = 3L,
            entries = listOf(
                BenefitCatalogEntry(
                    cardId = 1L,
                    benefitId = "dining",
                    title = "Dining rewards",
                    description = "Earn more on dining.",
                    categories = setOf("Dining", "Restaurants"),
                    rewardRatePercent = 5.0,
                    maxRewardAmount = 250.0,
                    minimumSpend = 100.0
                )
            )
        )

        val rules = BenefitCatalogRuleMapper.toRewardRules(snapshot)

        assertEquals(2, rules[1L]?.size)
        assertEquals(5.0, rules[1L]?.single { it.category == "Dining" }?.rewardRatePercent ?: 0.0, 0.001)
        assertEquals(250.0, rules[1L]?.single { it.category == "Dining" }?.maxRewardAmount ?: 0.0, 0.001)
    }

    @Test
    fun ignoresBenefitsWithoutRewardRate() {
        val snapshot = BenefitCatalogSnapshot(
            version = 1L,
            entries = listOf(
                BenefitCatalogEntry(1L, "info", "Airport lounge", "Lounge access")
            )
        )

        assertTrue(BenefitCatalogRuleMapper.toRewardRules(snapshot).isEmpty())
    }

    @Test
    fun keepsHighestRewardRuleForDuplicateCatalogCategory() {
        val snapshot = BenefitCatalogSnapshot(
            version = 2L,
            entries = listOf(
                BenefitCatalogEntry(1L, "low", "Low", "Low", setOf("Dining"), rewardRatePercent = 2.0),
                BenefitCatalogEntry(1L, "high", "High", "High", setOf(" dining "), rewardRatePercent = 5.0)
            )
        )

        val rules = BenefitCatalogRuleMapper.toRewardRules(snapshot)

        assertEquals(1, rules[1L]?.size)
        assertEquals(5.0, rules[1L]!!.single().rewardRatePercent, 0.001)
    }

    @Test
    fun explicitRulesOverrideCatalogByCategory() {
        val catalogue = mapOf(1L to listOf(RewardRule("Dining", 5.0), RewardRule("Travel", 3.0)))
        val explicit = mapOf(1L to listOf(RewardRule(" dining ", 2.0)))

        val merged = BenefitCatalogRuleMapper.merge(catalogue, explicit)

        assertEquals(2, merged[1L]?.size)
        assertEquals(listOf(3.0, 2.0), merged[1L]!!.map { it.rewardRatePercent })
    }

    @Test
    fun preservesExplicitRulesForCardsMissingFromCatalog() {
        val explicit = mapOf(2L to listOf(RewardRule("Travel", 4.0)))

        assertEquals(explicit, BenefitCatalogRuleMapper.merge(emptyMap(), explicit))
    }
}
