package com.restpartijen.api.product.repository

import com.restpartijen.api.shared.SupplierIdLookup

/**
 * Persistence for suppliers (F1; table SUPPLIERS in persistence, issue #36).
 *
 * Extends SupplierIdLookup, so the repository itself is what other features get for that contract (A15).
 * Supplier queries that only F1 needs are added here later, such as finding a supplier for US-01.
 */
interface SupplierRepository : SupplierIdLookup