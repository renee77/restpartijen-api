package com.restpartijen.api.product.repository

import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.dbQuery
import com.restpartijen.api.product.model.OpeningHours
import com.restpartijen.api.shared.SupplierSummary
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
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

    override suspend fun exists(supplierId: Long): Boolean = dbQuery {
        SuppliersTable.selectAll().where { SuppliersTable.id eq supplierId }.singleOrNull() != null
    }

    // Passes on to findByUserId, so the query that finds a user's supplier exists only once.
    override suspend fun lookupSupplierId(userId: Long): Long? = findByUserId(userId)?.id

    override suspend fun getOpeningHours(supplierId: Long): List<OpeningHours> = dbQuery {
        SupplierOpeningHoursTable
            .selectAll()
            .where { SupplierOpeningHoursTable.supplierId eq supplierId }
            .map {
                OpeningHours(
                    day = it[SupplierOpeningHoursTable.dayOfWeek],
                    opensAt = it[SupplierOpeningHoursTable.opensAt],
                    closesAt = it[SupplierOpeningHoursTable.closesAt],
                )
            }
            // In Kotlin, not in SQL: the column holds day names, so SQL would sort them alphabetically.
            .sortedBy { it.day }
    }

    // Replaces the whole week: a day that is not in [openingHours] is closed afterwards (§9.9).
    // Delete and insert share one dbQuery, so one transaction: if the insert fails, the old week stays.
    override suspend fun updateOpeningHours(supplierId: Long, openingHours: List<OpeningHours>) {
        dbQuery {
            // Locks the supplier row first, so two replacements of one week run one after the other. Without it,
            // both can insert the same day and one breaks the primary key; that only worked out through the
            // default retry of Exposed.
            SuppliersTable.selectAll().where { SuppliersTable.id eq supplierId }.forUpdate().toList()
            SupplierOpeningHoursTable.deleteWhere { SupplierOpeningHoursTable.supplierId eq supplierId }
            SupplierOpeningHoursTable.batchInsert(openingHours) { hours ->
                this[SupplierOpeningHoursTable.supplierId] = supplierId
                this[SupplierOpeningHoursTable.dayOfWeek] = hours.day
                this[SupplierOpeningHoursTable.opensAt] = hours.opensAt
                this[SupplierOpeningHoursTable.closesAt] = hours.closesAt
            }
        }
    }
}
