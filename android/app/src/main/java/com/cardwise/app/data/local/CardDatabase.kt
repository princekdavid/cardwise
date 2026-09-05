package com.cardwise.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CardEntity::class, CardBenefitEntity::class],
    version = 1,
    exportSchema = true
)
abstract class CardDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
}
