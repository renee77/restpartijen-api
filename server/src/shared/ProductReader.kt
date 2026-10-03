package com.restpartijen.api.shared

import kotlin.time.Instant

/**
 * Read access to surplus products for other features (K-1, K-6).
 *
 * Provided by: F1 (Eva).
 * Used by: F2 (Stefan) for search and reservation, F3 (Lonneke) for expiry and admin.
 *
 * Returns ProductView, never SurplusProduct: callers can read a product but not change it.
 * The caller passes the current time, so the caller's injected Clock decides what "now" is.
 */
interface ProductReader {

    /** The product with this id, or null if it does not exist or is REMOVED. */
    suspend fun findById(id: Long): ProductView?

    /** Products with status LISTED whose best-before moment is after [now]. */
    suspend fun findAvailable(now: Instant): List<ProductView>

    /** Products still LISTED whose best-before moment is at or before [now]. */
    suspend fun findExpiredListings(now: Instant): List<ProductView>

    /**
     * Products whose status is one of [statuses], for the admin overview (B-26).
     *
     * The caller decides which statuses it needs: the admin overview asks for every
     * status except REMOVED by default, and for REMOVED only when filtering on it.
     * An empty set returns an empty list.
     */
//    Alleen de verwijderde	findByStatus(setOf(REMOVED))	Alle partijen met status REMOVED
//    Alleen de gereserveerde	findByStatus(setOf(RESERVED))	Alle partijen met status RESERVED
//    Twee statussen samen	findByStatus(setOf(LISTED, RESERVED))	Alles wat LISTED of RESERVED is
//    Alle lopende partijen	findByStatus(ProductStatus.entries.toSet() - REMOVED)	Alles behalve REMOVED

    suspend fun findByStatus(statuses: Set<ProductStatus>): List<ProductView>
}
