package com.cardwise.app.data

import android.content.Context
import androidx.room.Room
import com.cardwise.app.data.local.CardDatabase
import com.cardwise.app.data.repository.RoomCardRepository
import com.cardwise.app.domain.repository.CardRepository

class AppContainer(context: Context) {
    private val database: CardDatabase by lazy {
        Room.databaseBuilder(context, CardDatabase::class.java, "cardwise.db")
            .build()
    }

    val cardRepository: CardRepository by lazy {
        RoomCardRepository(database)
    }
}
