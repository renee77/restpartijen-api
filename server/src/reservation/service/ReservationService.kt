package com.restpartijen.api.reservation.service

import com.restpartijen.api.reservation.model.Reservation
import com.restpartijen.api.reservation.repository.ReservationRepository
import com.restpartijen.api.shared.DomainRuleException
import com.restpartijen.api.shared.IllegalStateTransitionException
import com.restpartijen.api.shared.NotFoundException
import com.restpartijen.api.shared.PriceProvider
import com.restpartijen.api.shared.ProductReader
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.ProductStatusUpdater
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
    suspend fun reserve(productId: Long, collectorId: Long): Reservation {
        // 404: unknown, or REMOVED (findById returns null for REMOVED, B-32)
        val product = productReader.findById(productId)
            ?: throw NotFoundException("Product $productId not found")

        // 409: only a LISTED product can be reserved (§5.5)
        if (product.status != ProductStatus.LISTED) {
            throw IllegalStateTransitionException("Product $productId not listed")
        }

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
        return reservations.create(
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
}