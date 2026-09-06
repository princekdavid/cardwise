package com.cardwise.app.domain.scan

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpiQrParserTest {
    @Test
    fun parsesCompleteUpiPayment() {
        val result = UpiQrParser.parse(
            "upi://pay?pa=merchant%40upi&pn=Coffee%20House&am=125.50&cu=INR&tr=TX123&tn=Morning%20coffee"
        )

        assertTrue(result is UpiQrParseResult.Success)
        val payment = (result as UpiQrParseResult.Success).payment
        assertEquals("merchant@upi", payment.vpa)
        assertEquals("Coffee House", payment.merchantName)
        assertEquals(BigDecimal("125.50"), payment.amount)
        assertEquals("INR", payment.currency)
        assertEquals("TX123", payment.transactionReference)
        assertEquals("Morning coffee", payment.note)
    }

    @Test
    fun parsesPaymentWithoutAmount() {
        val result = UpiQrParser.parse("upi://pay?pa=merchant@upi&pn=Shop&cu=INR")

        assertTrue(result is UpiQrParseResult.Success)
        assertEquals(null, (result as UpiQrParseResult.Success).payment.amount)
    }

    @Test
    fun decodesEncodedValues() {
        val result = UpiQrParser.parse("upi://pay?pa=merchant%40upi&pn=Tea%2B%20Snacks&cu=INR&tn=A%26B")

        assertTrue(result is UpiQrParseResult.Success)
        val payment = (result as UpiQrParseResult.Success).payment
        assertEquals("Tea+ Snacks", payment.merchantName)
        assertEquals("A&B", payment.note)
    }

    @Test
    fun rejectsMissingVpa() {
        val result = UpiQrParser.parse("upi://pay?pn=Shop&cu=INR")
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.MISSING_VPA), result)
    }

    @Test
    fun rejectsInvalidVpa() {
        val result = UpiQrParser.parse("upi://pay?pa=not-a-vpa&cu=INR")
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.INVALID_VPA), result)
    }

    @Test
    fun rejectsNonUpiQr() {
        assertEquals(UpiQrParseResult.NotUpi, UpiQrParser.parse("https://example.com/pay"))
    }

    @Test
    fun rejectsMalformedQuery() {
        val result = UpiQrParser.parse("upi://pay?pa=merchant@upi&broken&cu=INR")
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.MALFORMED_URI), result)
    }

    @Test
    fun rejectsDuplicateParameters() {
        val result = UpiQrParser.parse("upi://pay?pa=merchant@upi&pa=other@upi&cu=INR")
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.DUPLICATE_PARAMETER), result)
    }

    @Test
    fun rejectsZeroAmount() {
        val result = UpiQrParser.parse("upi://pay?pa=merchant@upi&am=0&cu=INR")
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.INVALID_AMOUNT), result)
    }

    @Test
    fun rejectsNegativeAmount() {
        val result = UpiQrParser.parse("upi://pay?pa=merchant@upi&am=-10&cu=INR")
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.INVALID_AMOUNT), result)
    }

    @Test
    fun rejectsInvalidAmount() {
        val result = UpiQrParser.parse("upi://pay?pa=merchant@upi&am=abc&cu=INR")
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.INVALID_AMOUNT), result)
    }

    @Test
    fun rejectsMoreThanTwoDecimalPlaces() {
        val result = UpiQrParser.parse("upi://pay?pa=merchant@upi&am=10.123&cu=INR")
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.INVALID_AMOUNT), result)
    }

    @Test
    fun rejectsMissingCurrency() {
        val result = UpiQrParser.parse("upi://pay?pa=merchant@upi")
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.MISSING_CURRENCY), result)
    }

    @Test
    fun rejectsUnsupportedCurrency() {
        val result = UpiQrParser.parse("upi://pay?pa=merchant@upi&cu=USD")
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.UNSUPPORTED_CURRENCY), result)
    }

    @Test
    fun rejectsOversizedPayload() {
        val payload = "upi://pay?pa=merchant@upi&cu=INR&pn=" + "x".repeat(5000)
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.PAYLOAD_TOO_LONG), UpiQrParser.parse(payload))
    }

    @Test
    fun doesNotExposeUnknownParameters() {
        val result = UpiQrParser.parse("upi://pay?pa=merchant@upi&cu=INR&evil=secret")
        assertTrue(result is UpiQrParseResult.Success)
        val payment = (result as UpiQrParseResult.Success).payment
        assertEquals("merchant@upi", payment.vpa)
        assertEquals(null, payment.note)
    }
}
