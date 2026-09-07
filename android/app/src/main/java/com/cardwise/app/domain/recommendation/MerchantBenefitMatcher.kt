package com.cardwise.app.domain.recommendation

import com.cardwise.app.domain.model.BenefitCatalogEntry
import java.util.Locale

/**
 * Deterministically matches catalogue merchant hints against sanitized UPI merchant metadata.
 * Matching is local-only and never persists the scanned payment payload.
 */
object MerchantBenefitMatcher {
    data class Match(
        val benefit: BenefitCatalogEntry,
        val matchedHint: String,
        val score: Int
    )

    fun match(
        entries: List<BenefitCatalogEntry>,
        merchantName: String?,
        vpa: String?
    ): List<Match> {
        val merchant = normalize(merchantName)
        val paymentAddress = normalize(vpa)
        if (merchant.isEmpty() && paymentAddress.isEmpty()) return emptyList()

        return entries.asSequence()
            .mapNotNull { entry ->
                entry.merchantHints.asSequence()
                    .map { hint -> hint to score(hint, merchant, paymentAddress) }
                    .filter { it.second > 0 }
                    .maxWithOrNull(compareBy<Pair<String, Int>> { it.second }.thenBy { it.first })
                    ?.let { (hint, score) -> Match(entry, hint, score) }
            }
            .sortedWith(
                compareByDescending<Match> { it.score }
                    .thenByDescending { it.benefit.priority }
                    .thenBy { it.benefit.cardId }
                    .thenBy { it.benefit.benefitId }
            )
            .toList()
    }

    private fun score(hint: String, merchant: String, vpa: String): Int {
        val normalizedHint = normalize(hint)
        if (normalizedHint.isEmpty()) return 0

        return when {
            merchant == normalizedHint -> 100
            merchant.contains(normalizedHint) -> 80
            vpa == normalizedHint -> 90
            vpa.contains(normalizedHint) -> 70
            else -> 0
        }
    }

    private fun normalize(value: String?): String =
        value.orEmpty()
            .trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9@._-]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
}
