package com.restpartijen.api.testsupport

import com.restpartijen.api.shared.SupplierIdLookup

/** Fake of F1's SupplierIdLookup: fill [supplierIds] with user id to supplier id, and see who was asked in [asked]. */
class FakeSupplierIdLookup : SupplierIdLookup {
    val supplierIds = mutableMapOf<Long, Long>()
    val asked = mutableListOf<Long>()

    override suspend fun lookupSupplierId(userId: Long): Long? {
        asked += userId
        return supplierIds[userId]
    }
}