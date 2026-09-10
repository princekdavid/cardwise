package com.cardwise.app.domain.repository

interface OnboardingRepository {
    fun isCompleted(): Boolean
    fun complete()
    fun reset()
}
