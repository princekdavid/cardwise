package com.cardwise.app.domain.scan

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class UpiPaymentHandoffOfflineFirstTest {
    @Test
    fun buildUri_requiresNoNetworkAndUsesOnlyParsedPaymentFields() {
        val payment = UpiPaymentRequest(
            vpa = "merchant@upi",
            merchantName = "CardWise Shop",
            amount = BigDecimal("125.00"),
            currency = "INR",
            transactionReference = "ref-42",
            note = "Order 42",
            merchantCategory = "5812"
        )

        assertEquals(
            "upi://pay?pa=merchant%40upi&pn=CardWise+Shop&am=125&cu=INR&tr=ref-42&tn=Order+42",
            UpiPaymentHandoff.buildUri(payment)
        )
    }

    @Test
    fun buildUri_withMissingAmountStillWorksOffline() {
        val payment = UpiPaymentRequest(
            vpa = "merchant@upi",
            merchantName = "CardWise Shop",
            amount = null,
            currency = "INR"
        )

        assertEquals(
            "upi://pay?pa=merchant%40upi&pn=CardWise+Shop&cu=INR",
            UpiPaymentHandoff.buildUri(payment)
        )
    }
}
