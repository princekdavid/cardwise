package com.cardwise.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val issuer: String,
    val name: String,
    val lastFour: String,
    val network: String,
    val isActive: Boolean = true
)
