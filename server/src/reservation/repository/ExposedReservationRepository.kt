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
            it[ReservationsTable.collectedAt] = item.collectedAt
        }[ReservationsTable.id]
        item.copy(id = newId)
    }

    /** Stores only a new status and pickup moment; every other column keeps the value it got at reserving. */
    override suspend fun update(item: Reservation): Boolean = dbQuery {
        // Only the status and the pickup moment can change; price and the other times are fixed when reserving (§5.5).
        ReservationsTable.update({ ReservationsTable.id eq item.id }) {
            it[ReservationsTable.status] = item.status
            it[ReservationsTable.collectedAt] = item.collectedAt
        } > 0
    }

    /**
     * Required by Repository<T>, but reservations are never deleted: the history is kept through the status
     * (§9.5). This deliberately does not honour the interface's contract. No code should call it; ending a
     * reservation goes through ReservationService and the state machine. Throwing makes a mistaken call fail
     * at once.
     *
     * @throws UnsupportedOperationException always
     */
    override suspend fun delete(id: Long): Boolean = throw UnsupportedOperationException(
        "Reservations are never deleted; history is kept through the status. Cancel through ReservationService (§9.5).",
    )

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

/** Translates one row into the domain model. */
private fun ResultRow.toReservation(): Reservation = Reservation(
    id = this[ReservationsTable.id],
    productId = this[ReservationsTable.productId],
    collectorId = this[ReservationsTable.collectorId],
    reservedPrice = Money.fromCents(this[ReservationsTable.reservedPriceCents]),
    reservedAt = this[ReservationsTable.reservedAt],
    expiresAt = this[ReservationsTable.expiresAt],
    status = this[ReservationsTable.status],
    collectedAt = this[ReservationsTable.collectedAt],
)
