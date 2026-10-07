package com.restpartijen.api.pricing.service

import com.restpartijen.api.shared.*
import com.restpartijen.api.pricing.model.MaintenanceReport
import kotlin.time.Clock

class ExpiryScheduler (
    private val reservationMaintenance: ReservationMaintenance,
    private val productReader: ProductReader,
    private val productStatusUpdater: ProductStatusUpdater,
    private val clock: Clock
){
    suspend fun runMaintenance(): MaintenanceReport {
            // One moment for both steps, so the result never depends on when each step happens to run.
            val now = clock.now()

            // Step 1: lapse overdue reservations first; their products return to LISTED.
            val lapsed = reservationMaintenance.lapseOverdue(now)

            // Step 2: expire the listings whose best-before moment has passed.
            val expiredIds = productReader.findExpiredListings(now).map { it.id }
            val expired = productStatusUpdater.markExpired(expiredIds)

        if (expired < expiredIds.size) {
            throw IllegalStateTransitionException("Cannot reserve a reserved product")
        }


        return MaintenanceReport(lapsedReservations = lapsed, expiredProducts = expired)
        }
}