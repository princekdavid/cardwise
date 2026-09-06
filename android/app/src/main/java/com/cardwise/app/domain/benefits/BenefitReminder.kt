package com.cardwise.app.domain.benefits

/** Pure reminder decision for benefits that expire on a known date. */
data class BenefitReminder(
    val benefitId: Long,
    val expiresAtEpochDay: Long,
    val remindBeforeDays: Long = 7
) {
    init {
        require(benefitId > 0) { "Benefit id must be positive" }
        require(remindBeforeDays >= 0) { "Reminder window must not be negative" }
    }
}

object BenefitReminderPolicy {
    fun shouldRemind(
        reminder: BenefitReminder,
        todayEpochDay: Long
    ): Boolean {
        val daysUntilExpiry = reminder.expiresAtEpochDay - todayEpochDay
        return daysUntilExpiry in 0..reminder.remindBeforeDays
    }
}
