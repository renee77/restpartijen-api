package com.restpartijen.api.reservation.model

import com.restpartijen.api.shared.Money
import kotlin.time.Instant

/** How a reservation ended; it keeps that after the product is free again (§9.5). */
enum class ReservationStatus { ACTIVE, COLLECTED, CANCELLED, LAPSED }

/**
 * One reservation of a whole product by one collector (US-05).
 * The price is fixed at the moment of reserving and never changes afterwards (§5.5).
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
