package com.cardwise.app.domain.resource

/** Local cache boundary. Implementations persist normalized provider results for offline use. */
interface ResourceStore<T> {
    suspend fun replace(batch: ResourceBatch<T>)
    suspend fun read(): List<T>
}
