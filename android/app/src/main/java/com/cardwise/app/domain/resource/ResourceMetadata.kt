package com.cardwise.app.domain.resource

import java.time.Instant

/** Provenance and freshness attached to externally sourced CardWise resources. */
data class ResourceMetadata(
    val sourceId: String,
    val sourceType: ResourceSourceType,
    val sourceUri: String? = null,
    val verifiedAt: Instant? = null,
    val effectiveFrom: Instant? = null,
    val expiresAt: Instant? = null,
    val version: String? = null,
    val confidence: ResourceConfidence = ResourceConfidence.UNKNOWN
)

enum class ResourceSourceType {
    OFFICIAL_ISSUER,
    OFFICIAL_NETWORK,
    OFFICIAL_MERCHANT,
    PARTNER_FEED,
    AGGREGATOR,
    USER_PROVIDED,
    UNKNOWN
}

enum class ResourceConfidence {
    VERIFIED,
    HIGH,
    MEDIUM,
    LOW,
    UNKNOWN
}
