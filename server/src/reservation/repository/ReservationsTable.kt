package com.restpartijen.api.reservation.repository

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.reservation.model.ReservationStatus
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

/** Table for reservations (ERD §9.1). */
object ReservationsTable : Table("reservations") {
    /** The reservation id, chosen by the database. */
    val id = long("id").autoIncrement()

    /**
     * The reserved product. Both referenced tables live in persistence, so F2 may refer to them (issue #36).
     * The database refuses an unknown product.
     */
    val productId = long("product_id").references(ProductsTable.id)

    /** The user who reserved the product; the database refuses an unknown collector. */
    val collectorId = long("collector_id").references(UsersTable.id)

    /** Price at the moment of reserving, in whole cents (ADR-07, §5.5). */
    val reservedPriceCents = long("reserved_price_cents")

    /** When the product was reserved. */
    val reservedAt = timestamp("reserved_at")

    /** When the reservation lapses if it is not collected. */
    val expiresAt = timestamp("expires_at")

    /** When the product was collected; null until then. */
    val collectedAt = timestamp("collected_at").nullable()

    /** The reservation status, stored by name. */
    val status = enumerationByName<ReservationStatus>("status", length = 20)

    override val primaryKey = PrimaryKey(id)
}
