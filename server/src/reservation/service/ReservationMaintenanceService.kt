package com.restpartijen.api.reservation.service

import com.restpartijen.api.persistence.dbQuery
import com.restpartijen.api.reservation.model.Reservation
import com.restpartijen.api.reservation.model.ReservationEvent
import com.restpartijen.api.reservation.repository.ReservationRepository
import com.restpartijen.api.shared.IllegalStateTransitionException
import com.restpartijen.api.shared.ProductReader
import com.restpartijen.api.shared.ProductStatusUpdater
import com.restpartijen.api.shared.ReservationMaintenance
import kotlinx.coroutines.CancellationException
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.slf4j.LoggerFactory
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

    private val log = LoggerFactory.getLogger(ReservationMaintenanceService::class.java)

    override suspend fun lapseOverdue(now: Instant): Int = reservations.findOverdue(now).count { lapse(it) }

    override suspend fun lapseActiveFor(productId: Long): Boolean {
        val reservation = reservations.findActiveByProductId(productId) ?: return false
        return lapse(reservation)
    }

    /**
     * Lapses one reservation. Returns false if it could not lapse (K-5, issue #48): a missing product,
     * a refused transition or a failing write gives false instead of an exception, and the caller decides
     * what false means. Coroutine cancellation is still rethrown. On false nothing of this lapse is left
     * in the database.
     */
    private suspend fun lapse(reservation: Reservation): Boolean = dbQuery {
        val product = productReader.findById(reservation.productId) ?: return@dbQuery false

        val to = try {
            ReservationStateMachine.transition(ReservationEvent.LAPSE, product.status, reservation.status)
        } catch (e: IllegalStateTransitionException) {
            return@dbQuery false
        }

        // Two writes that belong together. The savepoint marks the moment before them, so a failure
        // undoes only these writes and we can still answer false instead of throwing. That also works
        // when we run inside the caller's open transaction (AdminProductService.delete).
        val connection = TransactionManager.current().connection
        val savepoint = connection.setSavepoint("lapse_reservation_${reservation.id}")
        val lapsed = try {
            productStatusUpdater.markListed(product.id) &&
                reservations.update(reservation.copy(status = to.reservation))
        } catch (e: CancellationException) {
            connection.rollback(savepoint)
            throw e // never swallow coroutine cancellation
        } catch (e: Exception) {
            log.warn("Reservation ${reservation.id} could not lapse", e)
            false
        }
        if (!lapsed) connection.rollback(savepoint)
        connection.releaseSavepoint(savepoint)
        lapsed
    }
}
