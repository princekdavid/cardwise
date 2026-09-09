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
    fun parsesMerchantCategoryCodeWithoutExposingRawPayload() {
        val result = UpiQrParser.parse("upi://pay?pa=merchant@upi&pn=Toit&am=2850&cu=INR&mc=5812")
        assertTrue(result is UpiQrParseResult.Success)
        assertEquals("5812", (result as UpiQrParseResult.Success).payment.merchantCategory)
    }

    @Test
    fun ignoresMalformedMerchantCategoryCode() {
        val result = UpiQrParser.parse("upi://pay?pa=merchant@upi&cu=INR&mc=dining")
        assertTrue(result is UpiQrParseResult.Success)
        assertEquals(null, (result as UpiQrParseResult.Success).payment.merchantCategory)
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
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.MISSING_VPA), UpiQrParser.parse("upi://pay?pn=Shop&cu=INR"))
    }

    @Test
    fun rejectsInvalidVpa() {
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.INVALID_VPA), UpiQrParser.parse("upi://pay?pa=not-a-vpa&cu=INR"))
    }

    @Test
    fun rejectsNonUpiQr() {
        assertEquals(UpiQrParseResult.NotUpi, UpiQrParser.parse("https://example.com/pay"))
    }

    @Test
    fun rejectsMalformedQuery() {
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.MALFORMED_URI), UpiQrParser.parse("upi://pay?pa=merchant@upi&broken&cu=INR"))
    }

    @Test
    fun rejectsDuplicateParameters() {
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.DUPLICATE_PARAMETER), UpiQrParser.parse("upi://pay?pa=merchant@upi&pa=other@upi&cu=INR"))
    }

    @Test
    fun rejectsZeroAmount() {
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.INVALID_AMOUNT), UpiQrParser.parse("upi://pay?pa=merchant@upi&am=0&cu=INR"))
    }

    @Test
    fun rejectsNegativeAmount() {
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.INVALID_AMOUNT), UpiQrParser.parse("upi://pay?pa=merchant@upi&am=-10&cu=INR"))
    }

    @Test
    fun rejectsInvalidAmount() {
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.INVALID_AMOUNT), UpiQrParser.parse("upi://pay?pa=merchant@upi&am=abc&cu=INR"))
    }

    @Test
    fun rejectsMoreThanTwoDecimalPlaces() {
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.INVALID_AMOUNT), UpiQrParser.parse("upi://pay?pa=merchant@upi&am=10.123&cu=INR"))
    }

    @Test
    fun rejectsMissingCurrency() {
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.MISSING_CURRENCY), UpiQrParser.parse("upi://pay?pa=merchant@upi"))
    }

    @Test
    fun rejectsUnsupportedCurrency() {
        assertEquals(UpiQrParseResult.Invalid(UpiQrParseResult.Reason.UNSUPPORTED_CURRENCY), UpiQrParser.parse("upi://pay?pa=merchant@upi&cu=USD"))
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
