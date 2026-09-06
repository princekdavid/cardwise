package com.cardwise.app.data.repository

import androidx.room.withTransaction
import com.cardwise.app.data.local.CardDatabase
import com.cardwise.app.data.local.RewardRuleEntity
import com.cardwise.app.domain.repository.RewardRuleRepository
import com.cardwise.app.domain.rewards.RewardRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomRewardRuleRepository(
    private val database: CardDatabase
) : RewardRuleRepository {
    private val dao = database.rewardRuleDao()

    override fun observeRules(): Flow<Map<Long, List<RewardRule>>> =
        dao.observeRules().map { entities ->
            entities.groupBy(RewardRuleEntity::cardId)
                .mapValues { (_, rules) -> rules.map(::toDomain) }
        }

    override suspend fun getRules(cardId: Long): List<RewardRule> =
        dao.getRules(cardId).map(::toDomain)

    override suspend fun replaceRules(cardId: Long, rules: List<RewardRule>) =
        database.withTransaction {
            dao.deleteRules(cardId)
            if (rules.isNotEmpty()) {
                dao.insertRules(rules.map { it.toEntity(cardId) })
            }
        }

    private fun toDomain(entity: RewardRuleEntity): RewardRule = RewardRule(
        category = entity.category,
        rewardRatePercent = entity.rewardRatePercent,
        maxRewardAmount = entity.maxRewardAmount,
        minimumSpend = entity.minimumSpend,
        maximumEligibleSpend = entity.maximumEligibleSpend,
        enabled = entity.enabled
    )

    private fun RewardRule.toEntity(cardId: Long): RewardRuleEntity = RewardRuleEntity(
        cardId = cardId,
        category = category,
        rewardRatePercent = rewardRatePercent,
        maxRewardAmount = maxRewardAmount,
        minimumSpend = minimumSpend,
        maximumEligibleSpend = maximumEligibleSpend,
        enabled = enabled
    )
}
