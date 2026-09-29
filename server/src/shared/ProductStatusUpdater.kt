package com.restpartijen.api.shared

/**
 * The only way for other features to change a product's status (K-1, K-6).
 * Provided by: F1 (Eva). Used by: F2 (Stefan) for reservations, F3 (Lonneke) for expiry and removal.
 */
interface ProductStatusUpdater {

    /** Sets RESERVED only if the product is still LISTED. Returns false if someone else was first. */
    suspend fun markReserved(productId: Long): Boolean

    /** Sets RESERVED back to LISTED (reservation cancelled or lapsed). */
    suspend fun markListed(productId: Long)

    /** Sets RESERVED to COLLECTED. */
    suspend fun markCollected(productId: Long)

    /** Sets the given LISTED products to EXPIRED. Returns how many changed. */
    suspend fun markExpired(productIds: List<Long>): Int

    /** Sets a LISTED product to REMOVED (decision point 3). Returns false if it was not LISTED. */
    suspend fun markRemoved(productId: Long): Boolean
}