package com.cardwise.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [CardEntity::class, CardBenefitEntity::class, RewardRuleEntity::class, BenefitCatalogEntity::class, BenefitCatalogMetadataEntity::class],
    version = 3,
    exportSchema = true
)
abstract class CardDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun rewardRuleDao(): RewardRuleDao
    abstract fun benefitCatalogDao(): BenefitCatalogDao

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
                        enabled INTEGER NOT NULL,
                        FOREIGN KEY(cardId) REFERENCES cards(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_reward_rules_cardId ON reward_rules(cardId)")
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS benefit_catalog (
                        cardId INTEGER NOT NULL,
                        benefitId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        categories TEXT NOT NULL,
                        merchantHints TEXT NOT NULL,
                        rewardRatePercent REAL,
                        maxRewardAmount REAL,
                        minimumSpend REAL NOT NULL,
                        maximumEligibleSpend REAL,
                        priority INTEGER NOT NULL,
                        catalogVersion INTEGER NOT NULL,
                        PRIMARY KEY(cardId, benefitId)
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_benefit_catalog_cardId ON benefit_catalog(cardId)")
                database.execSQL("CREATE TABLE IF NOT EXISTS benefit_catalog_metadata (id INTEGER NOT NULL PRIMARY KEY, version INTEGER NOT NULL)")
            }
        }
    }
}
