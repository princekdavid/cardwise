package com.cardwise.app.data

import android.content.Context
import androidx.room.Room
import com.cardwise.app.data.local.CardDatabase
import com.cardwise.app.data.repository.RoomCardRepository
import com.cardwise.app.data.repository.RoomPaymentHistoryRepository
import com.cardwise.app.data.repository.RoomPrivacyVaultRepository
import com.cardwise.app.data.repository.RoomRewardRuleRepository
import com.cardwise.app.data.repository.SharedPreferencesOnboardingRepository
import com.cardwise.app.domain.repository.CardRepository
import com.cardwise.app.domain.repository.OnboardingRepository
import com.cardwise.app.domain.repository.PaymentHistoryRepository
import com.cardwise.app.domain.repository.PrivacyVaultRepository
import com.cardwise.app.domain.repository.RewardRuleRepository

class AppContainer(context: Context) {
    private val database: CardDatabase by lazy {
        Room.databaseBuilder(context, CardDatabase::class.java, "cardwise.db")
            .addMigrations(CardDatabase.MIGRATION_1_2, CardDatabase.MIGRATION_2_3)
            .build()
    }

    val onboardingRepository: OnboardingRepository by lazy {
        SharedPreferencesOnboardingRepository(context)
    }

    val cardRepository: CardRepository by lazy {
        RoomCardRepository(database)
    }

    val rewardRuleRepository: RewardRuleRepository by lazy {
        RoomRewardRuleRepository(database)
    }

    val paymentHistoryRepository: PaymentHistoryRepository by lazy {
        RoomPaymentHistoryRepository(database)
    }

    val privacyVaultRepository: PrivacyVaultRepository by lazy {
        RoomPrivacyVaultRepository(database, onboardingRepository)
    }
}
