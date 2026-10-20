package com.restpartijen.api.product.repository

import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.dbQuery
import com.restpartijen.api.shared.SupplierSummary
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll

/** Suppliers in the database. */
class ExposedSupplierRepository : SupplierRepository {
    // user_id is unique (SuppliersTable), so there is one row or none.
    override suspend fun findByUserId(userId: Long): SupplierSummary? = dbQuery {
        SuppliersTable.selectAll().where { SuppliersTable.userId eq userId }.singleOrNull()
            ?.let {
                SupplierSummary(
                    id = it[SuppliersTable.id],
                    name = it[SuppliersTable.name],
                    latitude = it[SuppliersTable.latitude],
                    longitude = it[SuppliersTable.longitude],
                )
            }
    }

    // Passes on to findByUserId, so the query that finds a user's supplier exists only once.
    override suspend fun lookupSupplierId(userId: Long): Long? = findByUserId(userId)?.id
}