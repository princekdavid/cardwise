package com.cardwise.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Transaction
    @Query("SELECT * FROM cards ORDER BY id DESC")
    fun observeCards(): Flow<List<CardWithBenefits>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCard(card: CardEntity): Long

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("DELETE FROM card_benefits WHERE cardId = :cardId")
    suspend fun deleteBenefits(cardId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBenefits(benefits: List<CardBenefitEntity>)
}
