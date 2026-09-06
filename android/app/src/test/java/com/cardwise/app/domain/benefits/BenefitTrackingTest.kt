package com.cardwise.app.domain.benefits

import org.junit.Assert.assertEquals
import org.junit.Test

class BenefitTrackingTest {
    private val base = BenefitTracking(
        cardId = 1L,
        benefitId = 2L,
        usedAmount = 40.0,
        allowance = 100.0,
        periodStartEpochDay = 20_000L,
        periodEndEpochDay = 20_030L
    )

    @Test
    fun calculatesRemainingAllowance() {
        assertEquals(60.0, BenefitTracker.remaining(base)!!, 0.001)
    }

    @Test
    fun calculatesClampedProgress() {
        assertEquals(0.4, BenefitTracker.progress(base)!!, 0.001)
        assertEquals(1.0, BenefitTracker.progress(base.copy(usedAmount = 140.0))!!, 0.001)
    }

    @Test
    fun recordsUsageWithoutMutatingOriginal() {
        val updated = BenefitTracker.recordUsage(base, 15.0)
        assertEquals(55.0, updated.usedAmount, 0.001)
        assertEquals(40.0, base.usedAmount, 0.001)
    }

    @Test
    fun unlimitedAllowanceHasNoRemainingOrProgressValue() {
        val unlimited = base.copy(allowance = null)
        assertEquals(null, BenefitTracker.remaining(unlimited))
        assertEquals(null, BenefitTracker.progress(unlimited))
    }
}
