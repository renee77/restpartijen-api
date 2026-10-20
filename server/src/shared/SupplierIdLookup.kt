package com.restpartijen.api.shared

/**
 * Which supplier belongs to a user account (A15, decision 3.8).
 * Provided by: F1 (Eva). Used by: GI-3 (Lonneke), so POST /auth/login can return the supplierId.
 *
 * Returns only the id: the caller already has the user, and the supplier itself stays with F1.
 */
interface SupplierIdLookup {

    /** The id of the supplier of [userId], or null if this user has no supplier (a collector or an admin). */
    suspend fun lookupSupplierId(userId: Long): Long?
}
