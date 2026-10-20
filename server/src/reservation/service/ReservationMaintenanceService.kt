package com.restpartijen.api.reservation.service

import com.restpartijen.api.persistence.dbQuery
import com.restpartijen.api.reservation.model.Reservation
import com.restpartijen.api.reservation.model.ReservationEvent
import com.restpartijen.api.reservation.repository.ReservationRepository
import com.restpartijen.api.shared.IllegalStateTransitionException
import com.restpartijen.api.shared.ProductReader
import com.restpartijen.api.shared.ProductStatusUpdater
import com.restpartijen.api.shared.ReservationMaintenance
import kotlin.time.Instant

/**
 * F2's side of K-5: lapses reservations through the state machine (§9.5).
 * F3 decides when; this class decides which reservations.
 */
class ReservationMaintenanceService(
    private val productReader: ProductReader,
    private val productStatusUpdater: ProductStatusUpdater,
    private val reservations: ReservationRepository,
) : ReservationMaintenance {

    override suspend fun lapseOverdue(now: Instant): Int =
        reservations.findOverdue(now).count { lapse(it) }

    override suspend fun lapseActiveFor(productId: Long): Boolean {
        val reservation = reservations.findActiveByProductId(productId) ?: return false
        return lapse(reservation)
    }

    /** Lapses one reservation. Returns false if it could not lapse; the caller decides what that means. */
    private suspend fun lapse(reservation: Reservation): Boolean = dbQuery {
        val product = productReader.findById(reservation.productId) ?: return@dbQuery false

        val to = try {
            ReservationStateMachine.transition(ReservationEvent.LAPSE, product.status, reservation.status)
        } catch (e: IllegalStateTransitionException) {
            return@dbQuery false
        }

        if (!productStatusUpdater.markListed(product.id)) return@dbQuery false

        check(reservations.update(reservation.copy(status = to.reservation))) {
            "Reservation ${reservation.id} disappeared while lapsing"
        }
        true
    }
}