package com.cardwise.app.domain.scan

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpiPaymentHandoffTest {
    @Test
    fun buildsSanitizedUriWithAllowedFields() {
        val uri = UpiPaymentHandoff.buildUri(
            UpiPaymentRequest(
                vpa = "merchant@upi",
                merchantName = "Coffee & Co",
                amount = BigDecimal("125.50"),
                currency = "INR",
                transactionReference = "TX 123",
                note = "Morning coffee & snack"
            )
        )

        assertEquals(
            "upi://pay?pa=merchant%40upi&pn=Coffee+%26+Co&am=125.5&cu=INR&tr=TX+123&tn=Morning+coffee+%26+snack",
            uri
        )
    }

    @Test
    fun omitsOptionalFieldsWhenMissing() {
        val uri = UpiPaymentHandoff.buildUri(
            UpiPaymentRequest("merchant@upi", null, null, "INR", null, null)
        )

        assertEquals("upi://pay?pa=merchant%40upi&cu=INR", uri)
    }

    @Test
    fun neverForwardsUnknownFieldsOrRawPayload() {
        val payment = UpiPaymentRequest(
            vpa = "merchant@upi",
            merchantName = "Shop",
            amount = BigDecimal("10.00"),
            currency = "INR",
            transactionReference = null,
            note = "Pay"
        )
        val uri = UpiPaymentHandoff.buildUri(payment)

        assertFalse(uri.contains("evil"))
        assertFalse(uri.contains("rawPayload"))
        assertTrue(uri.startsWith("upi://pay?"))
    }

    @Test
    fun rejectsUnsupportedCurrency() {
        val payment = UpiPaymentRequest("merchant@upi", null, BigDecimal("10"), "USD", null, null)

        try {
            UpiPaymentHandoff.buildUri(payment)
            throw AssertionError("Expected unsupported currency to be rejected")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message!!.contains("INR"))
        }
    }

    @Test
    fun rejectsNonPositiveAmount() {
        val payment = UpiPaymentRequest("merchant@upi", null, BigDecimal.ZERO, "INR", null, null)

        try {
            UpiPaymentHandoff.buildUri(payment)
            throw AssertionError("Expected non-positive amount to be rejected")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message!!.contains("positive"))
        }
    }
}
