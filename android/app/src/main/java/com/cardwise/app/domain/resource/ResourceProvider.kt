package com.cardwise.app.domain.resource

/**
 * Provider boundary for dynamic CardWise resources.
 * Implementations may use official issuer/network APIs, partner feeds, or other verified sources.
 */
interface ResourceProvider<T> {
    val providerId: String

    suspend fun fetch(): ResourceBatch<T>
}

data class ResourceBatch<T>(
    val items: List<T>,
    val metadata: ResourceMetadata,
    val fetchedAtEpochMillis: Long
)
