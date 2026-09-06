package com.cardwise.app.domain.benefits

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BenefitReminderTest {
    private val reminder = BenefitReminder(
        benefitId = 2L,
        expiresAtEpochDay = 20_100L,
        remindBeforeDays = 7
    )

    @Test
    fun remindsInsideWindowIncludingExpiryDay() {
        assertTrue(BenefitReminderPolicy.shouldRemind(reminder, 20_093L))
        assertTrue(BenefitReminderPolicy.shouldRemind(reminder, 20_100L))
    }

    @Test
    fun doesNotRemindBeforeWindowOrAfterExpiry() {
        assertFalse(BenefitReminderPolicy.shouldRemind(reminder, 20_092L))
        assertFalse(BenefitReminderPolicy.shouldRemind(reminder, 20_101L))
    }
}
