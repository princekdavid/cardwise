package com.cardwise.app.domain.model

/** Canonical benefit metadata that can be refreshed without changing the user's wallet. */
data class BenefitCatalogEntry(
    val cardId: Long,
    val benefitId: String,
    val title: String,
    val description: String,
    val categories: Set<String> = emptySet(),
    val merchantHints: Set<String> = emptySet(),
    val rewardRatePercent: Double? = null,
    val maxRewardAmount: Double? = null,
    val minimumSpend: Double = 0.0,
    val maximumEligibleSpend: Double? = null,
    val priority: Int = 0
) {
    init {
        require(benefitId.isNotBlank())
        require(title.isNotBlank())
        require(description.isNotBlank())
        require(categories.all { it.isNotBlank() })
        require(merchantHints.all { it.isNotBlank() })
        rewardRatePercent?.let { require(it.isFinite() && it >= 0.0) }
        maxRewardAmount?.let { require(it.isFinite() && it >= 0.0) }
        require(minimumSpend.isFinite() && minimumSpend >= 0.0)
        maximumEligibleSpend?.let {
            require(it.isFinite() && it >= minimumSpend)
        }
    }
}

data class BenefitCatalogSnapshot(
    val version: Long,
    val entries: List<BenefitCatalogEntry>
)
