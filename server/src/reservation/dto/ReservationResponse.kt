package com.restpartijen.api.reservation.dto

import com.restpartijen.api.reservation.model.Reservation
import com.restpartijen.api.reservation.model.ReservationStatus
import com.restpartijen.api.shared.Money
import kotlinx.serialization.Serializable

/** Answer to a reservation. No collector e-mail or other personal data (§15). */
@Serializable
data class ReservationResponse(
    val id: Long,
    val productId: Long,
    val status: ReservationStatus,
    val reservedPrice: Double,
    val reservedAt: String,
    val expiresAt: String,
    // Only set once collected (US-11); null for every other status
    val collectedAt: String? = null,
)

fun Reservation.toResponse() = ReservationResponse(
    id = id,
    productId = productId,
    status = status,
    reservedPrice = reservedPrice.toEuros(),
    reservedAt = reservedAt.toString(),
    expiresAt = expiresAt.toString(),
    collectedAt = collectedAt?.toString(),
)
