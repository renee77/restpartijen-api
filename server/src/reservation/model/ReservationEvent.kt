package com.restpartijen.api.reservation.model

/** What can happen to a reservation (§9.5). */
enum class ReservationEvent { RESERVE, COLLECT, CANCEL, LAPSE }