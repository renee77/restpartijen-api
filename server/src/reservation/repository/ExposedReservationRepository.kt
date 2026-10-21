package com.restpartijen.api.reservation.repository

import com.restpartijen.api.persistence.dbQuery
import com.restpartijen.api.reservation.model.Reservation
import com.restpartijen.api.reservation.model.ReservationStatus
import com.restpartijen.api.shared.Money
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Instant

/**
 * Reservations in the database (§9.1).
 * Every function runs in dbQuery; inside a service's dbQuery it joins that transaction (GI-1, decision 1.4).
 */
class ExposedReservationRepository : ReservationRepository {

    override suspend fun findById(id: Long): Reservation? = dbQuery {
        ReservationsTable
            .selectAll()
            .where { ReservationsTable.id eq id }
            .singleOrNull()
            ?.toReservation()
    }

    override suspend fun findAll(): List<Reservation> = dbQuery {
        ReservationsTable
            .selectAll()
            .map { it.toReservation() }
    }

    override suspend fun create(item: Reservation): Reservation = dbQuery {
        // No id: the database chooses it, like in ExposedProductRepository.
        val newId = ReservationsTable.insert {
            it[ReservationsTable.productId] = item.productId
            it[ReservationsTable.reservedPriceCents] = item.reservedPrice.cents
            it[ReservationsTable.collectorId] = item.collectorId
            it[ReservationsTable.reservedAt] = item.reservedAt
            it[ReservationsTable.expiresAt] = item.expiresAt
            it[ReservationsTable.status] = item.status
        }[ReservationsTable.id]
        item.copy(id = newId)
    }

    override suspend fun update(item: Reservation): Boolean = dbQuery {
        // Only the status can change; price and times are fixed when reserving (§5.5).
        ReservationsTable.update({ ReservationsTable.id eq item.id }) {
            it[ReservationsTable.status] = item.status
        } > 0
    }

    override suspend fun delete(id: Long): Boolean = dbQuery {
        // Not used by F2 (history is kept through the status), but required by Repository<T>.
        ReservationsTable.deleteWhere { ReservationsTable.id eq id } > 0
    }

    override suspend fun findOverdue(now: Instant): List<Reservation> = dbQuery {
        ReservationsTable.selectAll().where {
            (ReservationsTable.status eq ReservationStatus.ACTIVE) and
                    (ReservationsTable.expiresAt lessEq now)
        }
            .map { it.toReservation() }
    }

    override suspend fun findActiveByProductId(productId: Long): Reservation? = dbQuery {
        ReservationsTable
            .selectAll()
            .where {
                (ReservationsTable.productId eq productId) and
                        (ReservationsTable.status eq ReservationStatus.ACTIVE)
            }
            .singleOrNull()
            ?.toReservation()
    }

    override suspend fun findByCollectorId(collectorId: Long): List<Reservation> = dbQuery {
        ReservationsTable
            .selectAll()
            .where(ReservationsTable.collectorId eq collectorId)
            .orderBy(ReservationsTable.reservedAt, SortOrder.DESC)
            .map { it.toReservation() }
    }
}

// Translates one row into the domain model.
private fun ResultRow.toReservation(): Reservation = Reservation(
    id = this[ReservationsTable.id],
    productId = this[ReservationsTable.productId],
    collectorId = this[ReservationsTable.collectorId],
    reservedPrice = Money.fromCents(this[ReservationsTable.reservedPriceCents]),
    reservedAt = this[ReservationsTable.reservedAt],
    expiresAt = this[ReservationsTable.expiresAt],
    status = this[ReservationsTable.status],
)