package com.cardwise.app.domain.scan

import java.math.BigDecimal
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Builds a sanitized UPI payment URI from parsed fields; raw QR payload is never forwarded. */
object UpiPaymentHandoff {
    private const val MAX_FIELD_LENGTH = 2048

    fun buildUri(payment: UpiPaymentRequest): String {
        val vpa = payment.vpa.trim()
        require(isValidVpa(vpa)) { "VPA is invalid" }
        require(payment.currency.equals("INR", ignoreCase = true)) { "Only INR payments are supported" }
        require(payment.amount == null || payment.amount > BigDecimal.ZERO) {
            "Amount must be positive when provided"
        }

        val parameters = buildList {
            add("pa" to vpa)
            payment.merchantName?.trim()?.takeIf(String::isNotBlank)?.let {
                require(isSafeField(it)) { "Merchant name is invalid" }
                add("pn" to it)
            }
            payment.amount?.let { add("am" to it.stripTrailingZeros().toPlainString()) }
            add("cu" to "INR")
            payment.transactionReference?.trim()?.takeIf(String::isNotBlank)?.let {
                require(isSafeField(it)) { "Transaction reference is invalid" }
                add("tr" to it)
            }
            payment.note?.trim()?.takeIf(String::isNotBlank)?.let {
                require(isSafeField(it)) { "Note is invalid" }
                add("tn" to it)
            }
        }

        return parameters.joinToString(prefix = "upi://pay?", separator = "&") { (key, value) ->
            "$key=${encode(value)}"
        }
    }

    private fun isValidVpa(vpa: String): Boolean {
        if (vpa.length !in 3..256 || vpa.any { it.isWhitespace() || it.isISOControl() }) return false
        val at = vpa.indexOf('@')
        return at in 1 until vpa.lastIndex && vpa.lastIndexOf('@') == at
    }

    private fun isSafeField(value: String): Boolean =
        value.length <= MAX_FIELD_LENGTH && value.none(Char::isISOControl)

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())
}
