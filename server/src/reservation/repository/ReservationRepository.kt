package com.restpartijen.api.reservation.repository

import com.restpartijen.api.persistence.Repository
import com.restpartijen.api.reservation.model.Reservation
import kotlin.time.Instant

/** Persistence of reservations; implements the generic contract from GI-1 (§11.2). */
interface ReservationRepository : Repository<Reservation> {
    /** Find active reservations whose expiresAt is at or before 'now' (5.8) **/
    suspend fun findOverdue(now: Instant): List<Reservation>

    /** Find active reservation of this product or null **/
    suspend fun findActiveByProductId(productId: Long): Reservation?

    /** All reservations of this collector, newest first. */
    suspend fun findByCollectorId(collectorId: Long): List<Reservation>
}