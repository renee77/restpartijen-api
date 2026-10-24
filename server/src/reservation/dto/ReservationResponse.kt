package com.restpartijen.api.reservation.dto

import com.restpartijen.api.reservation.model.Reservation
import com.restpartijen.api.reservation.model.ReservationStatus
import kotlinx.serialization.Serializable

/**
 * Answer to a reservation. No collector e-mail or other personal data (§15).
 *
 * @property id the reservation id
 * @property productId the reserved product
 * @property status the reservation status
 * @property reservedPrice the price fixed at the moment of reserving, in euros
 * @property reservedAt when the product was reserved, as an ISO-8601 instant
 * @property expiresAt when the reservation lapses if it is not collected, as an ISO-8601 instant
 * @property collectedAt when the product was collected; only set once collected (US-11), null for every other status
 */
@Serializable
data class ReservationResponse(
    val id: Long,
    val productId: Long,
    val status: ReservationStatus,
    val reservedPrice: Double,
    val reservedAt: String,
    val expiresAt: String,
    val collectedAt: String? = null,
)

/** Maps a reservation to its JSON answer: the price in euros and the moments as ISO-8601 text. */
fun Reservation.toResponse() = ReservationResponse(
    id = id,
    productId = productId,
    status = status,
    reservedPrice = reservedPrice.toEuros(),
    reservedAt = reservedAt.toString(),
    expiresAt = expiresAt.toString(),
    collectedAt = collectedAt?.toString(),
)
