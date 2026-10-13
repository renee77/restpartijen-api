package com.restpartijen.api.reservation.dto

import com.restpartijen.api.reservation.model.Reservation
import com.restpartijen.api.reservation.model.ReservationStatus
import kotlinx.serialization.Serializable

/** Answer to a reservation. No collector e-mail or other personal data (§15). */
@Serializable
data class ReservationResponse(
    val id: Long,
    val productId: Long,
    val status: ReservationStatus,
    val expiresAt: String,
)

fun Reservation.toResponse() = ReservationResponse(
    id = id,
    productId = productId,
    status = status,
    expiresAt = expiresAt.toString(),
)