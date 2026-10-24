package com.restpartijen.api.reservation.dto

import kotlinx.serialization.Serializable

/** POST /api/v1/reservations. A reservation is for the whole product, so there is no quantity (§5.5). */
@Serializable
data class CreateReservationRequest(val productId: Long)
