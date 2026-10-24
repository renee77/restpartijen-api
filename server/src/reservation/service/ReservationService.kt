package com.restpartijen.api.reservation.service

import com.restpartijen.api.persistence.dbQuery
import com.restpartijen.api.reservation.model.Reservation
import com.restpartijen.api.reservation.model.ReservationEvent
import com.restpartijen.api.reservation.repository.ReservationRepository
import com.restpartijen.api.shared.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

/**
 * Reserving a product (US-05). Talks to other features only through the contracts in shared (K-1, K-3).
 * Throws a DomainException for every refusal; StatusPages turns it into the status code (§10.2).
 */
class ReservationService(
    private val productReader: ProductReader,
    private val statusUpdater: ProductStatusUpdater,
    private val priceProvider: PriceProvider,
    private val reservations: ReservationRepository,
    private val clock: Clock,
) {
    suspend fun reserve(productId: Long, collectorId: Long): Reservation = dbQuery {
        // 404: unknown, or REMOVED (findById returns null for REMOVED, B-32)
        val product = productReader.findById(productId)
            ?: throw NotFoundException("Product $productId not found")

        // 409 for every combination outside §9.5; the state machine decides
        ReservationStateMachine.transition(ReservationEvent.RESERVE, product.status, reservation = null)

        // 409: still LISTED but past its best-before moment; PriceProvider returns null then
        val price = priceProvider.currentPrice(product)
            ?: throw IllegalStateTransitionException("Product $productId has expired")

        // 422: reserving is possible until the end of the pickup window (§5.5)
        val now = clock.now()
        if (now > product.pickupWindow.until) {
            throw DomainRuleException("The pickup window of product $productId has ended")
        }

        // 409: someone else was just first (markReserved only succeeds while still LISTED)
        if (!statusUpdater.markReserved(productId)) {
            throw IllegalStateTransitionException("Product $productId was just reserved by someone else")
        }

        // Valid for 24 hours, or until the pickup window ends if that is earlier (§5.5)
        val expiresAt = minOf(now + 24.hours, product.pickupWindow.until)
        reservations.create(
            Reservation(
                id = 0,
                productId = productId,
                collectorId = collectorId,
                reservedPrice = price,
                reservedAt = now,
                expiresAt = expiresAt,
            ),
        )
    }

    /** The collector's own reservations (§10.1). Who it is comes from the token, never from the request. */
    suspend fun findOwn(collectorId: Long): List<Reservation> = reservations.findByCollectorId(collectorId)

    /** Cancels an own reservation (US-06, §5.6): the product goes back to LISTED, the reservation to CANCELLED. */
    suspend fun cancel(reservationId: Long, collectorId: Long) = dbQuery {
        val reservation = reservations.findById(reservationId)
            ?: throw NotFoundException("Reservation $reservationId not found")

        if (reservation.collectorId != collectorId) {
            throw ForbiddenException("Reservation $reservationId is not allowed")
        }

        // The product is needed for the state machine; a REMOVED product has no active reservation left
        val product = productReader.findById(reservation.productId)
            ?: throw NotFoundException("Product ${reservation.productId} not found")

        // 409 for COLLECTED, CANCELLED or LAPSED; the state machine decides (§9.5)
        val to = ReservationStateMachine.transition(ReservationEvent.CANCEL, product.status, reservation.status)

        // 409: the product changed just now (markListed only succeeds while still RESERVED)
        if (!statusUpdater.markListed(product.id)) {
            throw IllegalStateTransitionException("Product ${product.id} is no longer reserved")
        }

        // After a write: throwing rolls back markListed too
        check(reservations.update(reservation.copy(status = to.reservation))) {
            "Reservation $reservationId disappeared while cancelling"
        }
    }

    /** Confirms the pickup of an own reservation (US-11, §5.11): both the reservation and the product become COLLECTED. */
    suspend fun collect(reservationId: Long, collectorId: Long): Reservation = dbQuery {
        val reservation = reservations.findById(reservationId)
            ?: throw NotFoundException("Reservation $reservationId not found")

        if (reservation.collectorId != collectorId) {
            throw ForbiddenException("Reservation $reservationId is not allowed")
        }

        val product = productReader.findById(reservation.productId)
            ?: throw NotFoundException("Product ${reservation.productId} not found")

        // 409 for COLLECTED, CANCELLED or LAPSED; the state machine decides (§9.5)
        val to = ReservationStateMachine.transition(ReservationEvent.COLLECT, product.status, reservation.status)

        // 422: only inside the pickup window (§5.11)
        val now = clock.now()
        if (now < product.pickupWindow.from || now > product.pickupWindow.until) {
            throw DomainRuleException("Reservation $reservationId can only be collected inside its pickup window")
        }

        // 409: the product changed just now (markCollected only succeeds while still RESERVED)
        if (!statusUpdater.markCollected(product.id)) {
            throw IllegalStateTransitionException("Product ${product.id} is no longer reserved")
        }

        val collected = reservation.copy(status = to.reservation, collectedAt = now)
        check(reservations.update(collected)) { "Reservation $reservationId disappeared while collecting" }
        collected
    }
}
