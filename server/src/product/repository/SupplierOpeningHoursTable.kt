package com.restpartijen.api.product.repository

import com.restpartijen.api.persistence.SuppliersTable
import kotlinx.datetime.DayOfWeek
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.time

/**
 * Table definition for the opening hours of a supplier (ERD §9.1, table SUPPLIER_OPENING_HOURS).
 *
 * One row per open day. A closed day has no row, so no column needs to be nullable (§9.9).
 */
object SupplierOpeningHoursTable : Table("supplier_opening_hours") {
    // The supplier these opening hours belong to; the database rejects an id that is not in SuppliersTable.
    val supplierId = long("supplier_id").references(SuppliersTable.id)

    // Stored by name (for example "MONDAY"); the longest name, "WEDNESDAY", has nine characters.
    val dayOfWeek = enumerationByName<DayOfWeek>("day_of_week", length = 9)

    // Local times in Europe/Amsterdam; PickupWindowCalculator turns them into instants (§9.9).
    val opensAt = time("opens_at")
    val closesAt = time("closes_at")

    // Composite key: a supplier has at most one row per day.
    override val primaryKey = PrimaryKey(supplierId, dayOfWeek)
}
