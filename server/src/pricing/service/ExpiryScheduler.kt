package com.restpartijen.api.pricing.service

import com.restpartijen.api.pricing.model.MaintenanceReport
import com.restpartijen.api.shared.ProductReader
import com.restpartijen.api.shared.ProductStatusUpdater
import com.restpartijen.api.shared.ReservationMaintenance
import org.slf4j.LoggerFactory
import kotlin.time.Clock

/**
 * Keeps product and reservation statuses up to date based on time (US-08).
 *
 * A run has two steps, and their order matters: first overdue reservations lapse,
 * which puts their products back to LISTED, and only then are listed products
 * past their best-before moment marked as expired. In the other order, a reserved
 * product that is also past its date would end up as LISTED again.
 *
 * Both steps use the same moment from the [Clock], and the run is idempotent:
 * a second run right after the first changes nothing and reports zero.
 */
class ExpiryScheduler(
    private val reservationMaintenance: ReservationMaintenance,
    private val productReader: ProductReader,
    private val productStatusUpdater: ProductStatusUpdater,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(ExpiryScheduler::class.java)

    suspend fun runMaintenance(): MaintenanceReport {
        // One moment for both steps, so the result never depends on when each step happens to run.
        val now = clock.now()

        // Step 1: lapse overdue reservations first; their products return to LISTED.
        val lapsed = reservationMaintenance.lapseOverdue(now)

        // Step 2: expire the listings whose best-before moment has passed.
        val expiredIds = productReader.findExpiredListings(now).map { it.id }
        val expired = productStatusUpdater.markExpired(expiredIds)

        // also: logs the report, then returns it unchanged
        return MaintenanceReport(lapsedReservations = lapsed, expiredProducts = expired)
            .also {
                log.info(
                    "Maintenance run: {} reservations lapsed, {} products expired",
                    it.lapsedReservations,
                    it.expiredProducts,
                )
            }
    }
}
