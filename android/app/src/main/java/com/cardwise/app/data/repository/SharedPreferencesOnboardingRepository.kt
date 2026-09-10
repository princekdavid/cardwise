package com.cardwise.app.data.repository

import android.content.Context
import com.cardwise.app.domain.repository.OnboardingRepository

class SharedPreferencesOnboardingRepository(context: Context) : OnboardingRepository {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun isCompleted(): Boolean = preferences.getBoolean(KEY_COMPLETED, false)

    override fun complete() {
        preferences.edit().putBoolean(KEY_COMPLETED, true).apply()
    }

    override fun reset() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "cardwise_onboarding"
        const val KEY_COMPLETED = "completed"
    }
}
