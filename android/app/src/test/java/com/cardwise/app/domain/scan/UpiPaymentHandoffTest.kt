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
    fun normalizesWhitespaceAndAmountScale() {
        val uri = UpiPaymentHandoff.buildUri(
            UpiPaymentRequest(
                vpa = " merchant@upi ",
                merchantName = " Merchant ",
                amount = BigDecimal("100.0000"),
                currency = "inr",
                transactionReference = " ref ",
                note = " note "
            )
        )

        assertEquals(
            "upi://pay?pa=merchant%40upi&pn=Merchant&am=100&cu=INR&tr=ref&tn=note",
            uri
        )
    }

    @Test
    fun omitsOptionalFieldsWhenMissingOrBlank() {
        val uri = UpiPaymentHandoff.buildUri(
            UpiPaymentRequest("merchant@upi", " ", null, "INR", "", "  ")
        )

        assertEquals("upi://pay?pa=merchant%40upi&cu=INR", uri)
    }

    @Test
    fun encodesReservedCharactersWithoutCreatingNewParameters() {
        val uri = UpiPaymentHandoff.buildUri(
            UpiPaymentRequest(
                vpa = "merchant@upi",
                merchantName = "Shop & Cafe?",
                amount = BigDecimal("10.00"),
                currency = "INR",
                transactionReference = "a=b&c",
                note = "hello?x=1&y=2"
            )
        )

        assertEquals(
            "upi://pay?pa=merchant%40upi&pn=Shop+%26+Cafe%3F&am=10&cu=INR&tr=a%3Db%26c&tn=hello%3Fx%3D1%26y%3D2",
            uri
        )
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
        assertFalse(uri.contains("foo="))
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

    @Test
    fun rejectsMoreThanTwoDecimalPlaces() {
        val payment = UpiPaymentRequest("merchant@upi", null, BigDecimal("10.123"), "INR", null, null)

        try {
            UpiPaymentHandoff.buildUri(payment)
            throw AssertionError("Expected amount precision to be rejected")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message!!.contains("two decimal places"))
        }
    }

    @Test
    fun rejectsMalformedVpa() {
        val invalidVpas = listOf("merchant", "@upi", "merchant@upi@extra", "merchant @upi")

        invalidVpas.forEach { vpa ->
            try {
                UpiPaymentHandoff.buildUri(UpiPaymentRequest(vpa, null, null, "INR", null, null))
                throw AssertionError("Expected malformed VPA to be rejected: $vpa")
            } catch (expected: IllegalArgumentException) {
                assertTrue(expected.message!!.contains("VPA"))
            }
        }
    }

    @Test
    fun rejectsControlCharactersInOptionalFields() {
        val invalidPayments = listOf(
            UpiPaymentRequest("merchant@upi", "Shop\nName", null, "INR", null, null),
            UpiPaymentRequest("merchant@upi", null, null, "INR", "ref\u0000", null),
            UpiPaymentRequest("merchant@upi", null, null, "INR", null, "note\r")
        )

        invalidPayments.forEach { payment ->
            try {
                UpiPaymentHandoff.buildUri(payment)
                throw AssertionError("Expected control character to be rejected")
            } catch (expected: IllegalArgumentException) {
                assertTrue(expected.message!!.contains("invalid"))
            }
        }
    }

    @Test
    fun rejectsOversizedOptionalFields() {
        val oversized = "x".repeat(2049)
        val payment = UpiPaymentRequest("merchant@upi", oversized, null, "INR", null, null)

        try {
            UpiPaymentHandoff.buildUri(payment)
            throw AssertionError("Expected oversized merchant name to be rejected")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message!!.contains("invalid"))
        }
    }
}
