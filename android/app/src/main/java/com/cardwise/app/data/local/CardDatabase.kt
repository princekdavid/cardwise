package com.cardwise.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [CardEntity::class, CardBenefitEntity::class, RewardRuleEntity::class, PaymentHistoryEntity::class],
    version = 3,
    exportSchema = true
)
abstract class CardDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun rewardRuleDao(): RewardRuleDao
    abstract fun paymentHistoryDao(): PaymentHistoryDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS reward_rules (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cardId INTEGER NOT NULL,
                        category TEXT NOT NULL,
                        rewardRatePercent REAL NOT NULL,
                        maxRewardAmount REAL,
                        minimumSpend REAL NOT NULL,
                        maximumEligibleSpend REAL,
                        enabled INTEGER NOT NULL,
                        FOREIGN KEY(cardId) REFERENCES cards(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_reward_rules_cardId ON reward_rules(cardId)")
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS payment_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        occurredAtEpochMillis INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        category TEXT NOT NULL,
                        cardId INTEGER NOT NULL,
                        rewardAmount REAL NOT NULL,
                        outcome TEXT NOT NULL
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_payment_history_occurredAtEpochMillis ON payment_history(occurredAtEpochMillis)")
            }
        }
    }
}
