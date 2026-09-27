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

    /** The product with this id, or null if it does not exist. */
    suspend fun findById(id: Long): ProductView?

    /** Products with status LISTED whose best-before moment is after [now]. */
    suspend fun findAvailable(now: Instant): List<ProductView>

    /** Products still LISTED whose best-before moment is at or before [now]. */
    suspend fun findExpiredListings(now: Instant): List<ProductView>

    /** All products in every status, for the admin overview. */
    // na kijken of REMOVED ook getoond wordt wij denken van niet
    suspend fun findAll(): List<ProductView>
}
