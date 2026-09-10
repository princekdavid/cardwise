package com.cardwise.app.data.repository

import androidx.room.RoomDatabase
import com.cardwise.app.domain.repository.PrivacyVaultRepository

class RoomPrivacyVaultRepository(
    private val database: RoomDatabase,
    private val onboardingRepository: com.cardwise.app.domain.repository.OnboardingRepository
) : PrivacyVaultRepository {
    override suspend fun resetAllData() {
        database.clearAllTables()
        onboardingRepository.reset()
    }
}
