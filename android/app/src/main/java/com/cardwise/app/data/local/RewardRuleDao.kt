package com.cardwise.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardRuleDao {
    @Query("SELECT * FROM reward_rules ORDER BY id ASC")
    fun observeRules(): Flow<List<RewardRuleEntity>>

    @Query("SELECT * FROM reward_rules WHERE cardId = :cardId ORDER BY id ASC")
    suspend fun getRules(cardId: Long): List<RewardRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<RewardRuleEntity>)

    @Query("DELETE FROM reward_rules WHERE cardId = :cardId")
    suspend fun deleteRules(cardId: Long)
}
