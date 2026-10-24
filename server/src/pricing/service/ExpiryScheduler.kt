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
 *
 * @property reservationMaintenance lapses the overdue reservations (F2's side of K-5)
 * @property productReader finds the listed products past their best-before moment
 * @property productStatusUpdater marks those products as EXPIRED
 * @property clock decides what "now" is for a run
 */
class ExpiryScheduler(
    private val reservationMaintenance: ReservationMaintenance,
    private val productReader: ProductReader,
    private val productStatusUpdater: ProductStatusUpdater,
    private val clock: Clock,
) {
    /** Logs the result of every run. */
    private val log = LoggerFactory.getLogger(ExpiryScheduler::class.java)

    /**
     * Runs both steps once and returns how many state changes each made.
     * Called by the periodic maintenance loop and by the admin endpoint.
     */
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
