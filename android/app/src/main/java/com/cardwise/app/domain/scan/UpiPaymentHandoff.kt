package com.cardwise.app.domain.scan

import java.math.BigDecimal
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Builds a sanitized UPI payment URI from parsed fields; raw QR payload is never forwarded. */
object UpiPaymentHandoff {
    fun buildUri(payment: UpiPaymentRequest): String {
        require(payment.vpa.isNotBlank()) { "VPA must not be blank" }
        require(payment.currency.equals("INR", ignoreCase = true)) { "Only INR payments are supported" }
        require(payment.amount == null || payment.amount > BigDecimal.ZERO) {
            "Amount must be positive when provided"
        }

        val parameters = buildList {
            add("pa" to payment.vpa)
            payment.merchantName?.takeIf(String::isNotBlank)?.let { add("pn" to it) }
            payment.amount?.let { add("am" to it.stripTrailingZeros().toPlainString()) }
            add("cu" to "INR")
            payment.transactionReference?.takeIf(String::isNotBlank)?.let { add("tr" to it) }
            payment.note?.takeIf(String::isNotBlank)?.let { add("tn" to it) }
        }

        return parameters.joinToString(prefix = "upi://pay?", separator = "&") { (key, value) ->
            "$key=${encode(value)}"
        }
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())
}
