package com.cardwise.app.domain.scan

import java.math.BigDecimal
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Builds a sanitized UPI payment URI from parsed fields; raw QR payload is never forwarded. */
object UpiPaymentHandoff {
    private const val MAX_FIELD_LENGTH = 2048
    private const val MAX_AMOUNT_SCALE = 2

    fun buildUri(payment: UpiPaymentRequest): String {
        val vpa = payment.vpa.trim()
        require(isValidVpa(vpa)) { "VPA is invalid" }
        require(payment.currency.equals("INR", ignoreCase = true)) { "Only INR payments are supported" }
        require(payment.amount == null || (payment.amount > BigDecimal.ZERO && payment.amount.scale() <= MAX_AMOUNT_SCALE)) {
            "Amount must be positive and have at most two decimal places when provided"
        }

        val parameters = buildList {
            add("pa" to vpa)
            sanitizedOptionalField(payment.merchantName, "Merchant name")?.let {
                add("pn" to it)
            }
            payment.amount?.let { add("am" to it.stripTrailingZeros().toPlainString()) }
            add("cu" to "INR")
            sanitizedOptionalField(payment.transactionReference, "Transaction reference")?.let {
                add("tr" to it)
            }
            sanitizedOptionalField(payment.note, "Note")?.let {
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

    private fun sanitizedOptionalField(value: String?, fieldName: String): String? {
        if (value == null) return null
        require(isSafeField(value)) { "$fieldName is invalid" }
        return value.trim().takeIf(String::isNotBlank)
    }

    private fun isSafeField(value: String): Boolean =
        value.length <= MAX_FIELD_LENGTH && value.none(Char::isISOControl)

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())
}
