package com.cardwise.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentHistoryDao {
    @Query("SELECT * FROM payment_history ORDER BY occurredAtEpochMillis DESC")
    fun observeEntries(): Flow<List<PaymentHistoryEntity>>

    @Insert
    suspend fun insert(entry: PaymentHistoryEntity)

    @Query("DELETE FROM payment_history")
    suspend fun clear()
}
