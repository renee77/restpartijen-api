package com.restpartijen.api.reservation.model

import com.restpartijen.api.shared.Money
import kotlin.time.Instant

/** How a reservation ended; it keeps that after the product is free again (§9.5). */
enum class ReservationStatus {
    /** Reserved and not ended yet. */
    ACTIVE,

    /** The collector picked the product up. */
    COLLECTED,

    /** The collector cancelled it. */
    CANCELLED,

    /** It ran out, or an admin removed the product. */
    LAPSED,
}

/**
 * One reservation of a whole product by one collector (US-05).
 * The price is fixed at the moment of reserving and never changes afterwards (§5.5).
 *
 * @property id the reservation id; 0 for a reservation that is not stored yet
 * @property productId the reserved product
 * @property collectorId the user who reserved it
 * @property reservedPrice the current price at the moment of reserving
 * @property reservedAt when the product was reserved
 * @property expiresAt when the reservation lapses: 24 hours after reserving, or the end of the pickup window
 *           if that is earlier (§5.5)
 * @property status where the reservation is in its life cycle
 * @property collectedAt when the product was collected; null until then (US-11)
 */
data class Reservation(
    val id: Long,
    val productId: Long,
    val collectorId: Long,
    val reservedPrice: Money,
    val reservedAt: Instant,
    val expiresAt: Instant,
    val status: ReservationStatus = ReservationStatus.ACTIVE,
    val collectedAt: Instant? = null,
)
