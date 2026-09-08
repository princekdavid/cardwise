package com.cardwise.app.domain.resource

/** Coordinates refreshes across dynamic resource providers and updates the local cache. */
class ResourceSyncEngine<T>(
    private val providers: List<ResourceProvider<T>>,
    private val store: ResourceStore<T>
) {
    suspend fun refresh(): ResourceSyncResult {
        if (providers.isEmpty()) return ResourceSyncResult.NoProviders

        val successful = mutableListOf<ResourceBatch<T>>()
        val failures = mutableListOf<ResourceProviderFailure>()

        providers.forEach { provider ->
            runCatching { provider.fetch() }
                .onSuccess(successful::add)
                .onFailure { error -> failures += ResourceProviderFailure(provider.providerId, error) }
        }

        if (successful.isEmpty()) return ResourceSyncResult.Failed(failures)

        val merged = successful.flatMap { it.items }.distinct()
        val newest = successful.maxBy { it.fetchedAtEpochMillis }
        store.replace(newest.copy(items = merged))

        return ResourceSyncResult.Success(
            itemCount = merged.size,
            providerCount = successful.size,
            failures = failures
        )
    }
}

data class ResourceProviderFailure(
    val providerId: String,
    val error: Throwable
)

sealed interface ResourceSyncResult {
    data object NoProviders : ResourceSyncResult

    data class Success(
        val itemCount: Int,
        val providerCount: Int,
        val failures: List<ResourceProviderFailure>
    ) : ResourceSyncResult

    data class Failed(val failures: List<ResourceProviderFailure>) : ResourceSyncResult
}
