package com.cardwise.app.domain.repository

import com.cardwise.app.domain.rewards.RewardRule
import kotlinx.coroutines.flow.Flow

interface RewardRuleRepository {
    fun observeRules(): Flow<Map<Long, List<RewardRule>>>
    suspend fun getRules(cardId: Long): List<RewardRule>
    suspend fun replaceRules(cardId: Long, rules: List<RewardRule>)
}
