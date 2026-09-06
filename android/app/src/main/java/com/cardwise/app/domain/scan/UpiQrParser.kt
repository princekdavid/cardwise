package com.cardwise.app.domain.scan

import java.math.BigDecimal
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/** Parsed payment data extracted from a UPI payment QR without retaining the raw payload. */
data class UpiPaymentRequest(
    val vpa: String,
    val merchantName: String?,
    val amount: BigDecimal?,
    val currency: String,
    val transactionReference: String?,
    val note: String?
)

sealed interface UpiQrParseResult {
    data class Success(val payment: UpiPaymentRequest) : UpiQrParseResult
    data object NotUpi : UpiQrParseResult
    data class Invalid(val reason: Reason) : UpiQrParseResult

    enum class Reason {
        EMPTY_PAYLOAD,
        PAYLOAD_TOO_LONG,
        MALFORMED_URI,
        MISSING_VPA,
        INVALID_VPA,
        DUPLICATE_PARAMETER,
        INVALID_AMOUNT,
        MISSING_CURRENCY,
        UNSUPPORTED_CURRENCY
    }
}

object UpiQrParser {
    private const val MAX_PAYLOAD_LENGTH = 4096
    private const val MAX_PARAMETER_LENGTH = 2048
    private const val SUPPORTED_CURRENCY = "INR"

    fun parse(rawPayload: String): UpiQrParseResult {
        if (rawPayload.isBlank()) return UpiQrParseResult.Invalid(UpiQrParseResult.Reason.EMPTY_PAYLOAD)
        if (rawPayload.length > MAX_PAYLOAD_LENGTH) {
            return UpiQrParseResult.Invalid(UpiQrParseResult.Reason.PAYLOAD_TOO_LONG)
        }

        val payload = rawPayload.trim()
        val schemeEnd = payload.indexOf(":")
        if (schemeEnd <= 0 || !payload.substring(0, schemeEnd).equals("upi", ignoreCase = true)) {
            return UpiQrParseResult.NotUpi
        }
        if (!payload.startsWith("upi://pay", ignoreCase = true)) {
            return UpiQrParseResult.NotUpi
        }

        val queryStart = payload.indexOf('?')
        if (queryStart < 0 || queryStart == payload.lastIndex) {
            return UpiQrParseResult.Invalid(UpiQrParseResult.Reason.MISSING_VPA)
        }

        val rawQuery = payload.substring(queryStart + 1)
        val parameters = linkedMapOf<String, String>()
        for (component in rawQuery.split('&')) {
            if (component.isEmpty()) continue
            val separator = component.indexOf('=')
            if (separator <= 0) return UpiQrParseResult.Invalid(UpiQrParseResult.Reason.MALFORMED_URI)
            val key = decode(component.substring(0, separator))
                ?: return UpiQrParseResult.Invalid(UpiQrParseResult.Reason.MALFORMED_URI)
            val value = decode(component.substring(separator + 1))
                ?: return UpiQrParseResult.Invalid(UpiQrParseResult.Reason.MALFORMED_URI)
            if (key.isBlank() || value.length > MAX_PARAMETER_LENGTH) {
                return UpiQrParseResult.Invalid(UpiQrParseResult.Reason.MALFORMED_URI)
            }
            if (parameters.put(key.lowercase(), value) != null) {
                return UpiQrParseResult.Invalid(UpiQrParseResult.Reason.DUPLICATE_PARAMETER)
            }
        }

        val vpa = parameters["pa"]?.trim()
            ?: return UpiQrParseResult.Invalid(UpiQrParseResult.Reason.MISSING_VPA)
        if (!isValidVpa(vpa)) return UpiQrParseResult.Invalid(UpiQrParseResult.Reason.INVALID_VPA)

        val currency = parameters["cu"]?.trim()?.uppercase()
            ?: return UpiQrParseResult.Invalid(UpiQrParseResult.Reason.MISSING_CURRENCY)
        if (currency != SUPPORTED_CURRENCY) {
            return UpiQrParseResult.Invalid(UpiQrParseResult.Reason.UNSUPPORTED_CURRENCY)
        }

        val amount = parameters["am"]?.trim()?.let { value ->
            val parsed = value.toBigDecimalOrNull()
            if (parsed == null || parsed <= BigDecimal.ZERO || parsed.scale() > 2) {
                return UpiQrParseResult.Invalid(UpiQrParseResult.Reason.INVALID_AMOUNT)
            }
            parsed
        }

        return UpiQrParseResult.Success(
            UpiPaymentRequest(
                vpa = vpa,
                merchantName = parameters["pn"]?.trim()?.takeIf(String::isNotEmpty),
                amount = amount,
                currency = currency,
                transactionReference = parameters["tr"]?.trim()?.takeIf(String::isNotEmpty),
                note = parameters["tn"]?.trim()?.takeIf(String::isNotEmpty)
            )
        )
    }

    private fun isValidVpa(vpa: String): Boolean {
        if (vpa.length !in 3..256 || vpa.any { it.isWhitespace() || it.isISOControl() }) return false
        val at = vpa.indexOf('@')
        return at in 1 until vpa.lastIndex && vpa.lastIndexOf('@') == at
    }

    private fun decode(value: String): String? = runCatching {
        URLDecoder.decode(value, StandardCharsets.UTF_8.name())
    }.getOrNull()
}
