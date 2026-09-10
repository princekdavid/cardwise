package com.cardwise.app.domain.repository

interface PrivacyVaultRepository {
    suspend fun resetAllData()
}
