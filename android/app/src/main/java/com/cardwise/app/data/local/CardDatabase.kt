package com.cardwise.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [CardEntity::class, CardBenefitEntity::class, RewardRuleEntity::class],
    version = 2,
    exportSchema = true
)
abstract class CardDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun rewardRuleDao(): RewardRuleDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS reward_rules (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cardId INTEGER NOT NULL,
                        category TEXT NOT NULL,
                        rewardRatePercent REAL NOT NULL,
                        maxRewardAmount REAL,
                        minimumSpend REAL NOT NULL,
                        maximumEligibleSpend REAL,
                        enabled INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_reward_rules_cardId ON reward_rules(cardId)"
                )
            }
        }
    }
}
