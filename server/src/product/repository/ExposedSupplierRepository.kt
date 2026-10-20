package com.restpartijen.api.product.repository

import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.dbQuery
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select

/** Suppliers in the database. */
class ExposedSupplierRepository : SupplierRepository {
    // Selects only the id column: the contract returns nothing else.
    // user_id is unique (SuppliersTable), so there is one row or none.
    override suspend fun lookupSupplierId(userId: Long): Long? = dbQuery {
        SuppliersTable.select(SuppliersTable.id).where { SuppliersTable.userId eq userId }.singleOrNull()
            ?.let { it[SuppliersTable.id] }
    }
}