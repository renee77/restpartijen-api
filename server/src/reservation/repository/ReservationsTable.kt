package com.restpartijen.api.reservation.repository

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.reservation.model.ReservationStatus
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

/** Table for reservations (ERD §9.1). */
object ReservationsTable : Table("reservations") {
    val id = long("id").autoIncrement()

    // Both tables live in persistence, so F2 may refer to them (issue #36).
    // The database refuses an unknown product or collector.
    val productId = long("product_id").references(ProductsTable.id)
    val collectorId = long("collector_id").references(UsersTable.id)

    // Price at the moment of reserving, in whole cents (ADR-07, §5.5).
    val reservedPriceCents = long("reserved_price_cents")
    val reservedAt = timestamp("reserved_at")
    val expiresAt = timestamp("expires_at")
    val collectedAt = timestamp("collected_at").nullable()
    val status = enumerationByName<ReservationStatus>("status", length = 20)

    override val primaryKey = PrimaryKey(id)
}
