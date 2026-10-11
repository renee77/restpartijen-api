package com.restpartijen.api.reservation.repository

import com.restpartijen.api.reservation.model.ReservationStatus
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

/** Table for reservations (ERD §9.1). */
object ReservationsTable : Table("reservations") {
    val id = long("id").autoIncrement()

    // No Exposed reference to ProductsTable: F2 may not import F1's package (§8.3, GI-5).
    val productId = long("product_id")
    val collectorId = long("collector_id")

    // Price at the moment of reserving, in whole cents (ADR-08, §5.5).
    val reservedPriceCents = long("reserved_price_cents")
    val reservedAt = timestamp("reserved_at")
    val expiresAt = timestamp("expires_at")
    val status = enumerationByName<ReservationStatus>("status", length = 20)

    override val primaryKey = PrimaryKey(id)
}