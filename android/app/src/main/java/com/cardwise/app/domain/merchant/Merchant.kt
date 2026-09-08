package com.cardwise.app.domain.merchant

import com.cardwise.app.domain.resource.ResourceMetadata

/** Merchant identity resolved from QR/payment context or a provider. */
data class Merchant(
    val merchantId: String,
    val displayName: String,
    val category: String? = null,
    val mcc: String? = null,
    val confidence: MerchantConfidence = MerchantConfidence.UNKNOWN,
    val metadata: ResourceMetadata
)

enum class MerchantConfidence {
    VERIFIED,
    INFERRED,
    UNKNOWN
}
