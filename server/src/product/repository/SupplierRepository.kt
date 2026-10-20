package com.restpartijen.api.product.repository

import com.restpartijen.api.shared.SupplierIdLookup
import com.restpartijen.api.shared.SupplierSummary

/**
 * Persistence for suppliers (F1; table SUPPLIERS in persistence, issue #36).
 *
 * Extends SupplierIdLookup, so the repository itself is what other features get for that contract (A15).
 * Supplier queries that only F1 needs live here too.
 */
interface SupplierRepository : SupplierIdLookup {

    /** The supplier of [userId] as a summary, or null if this user has no supplier. Used by US-01. */
    suspend fun findByUserId(userId: Long): SupplierSummary?
}