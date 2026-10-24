package com.restpartijen.api.reservation.model

/** What can happen to a reservation (§9.5). */
enum class ReservationEvent {
    /** A collector reserves a listed product. */
    RESERVE,

    /** The collector picks the product up. */
    COLLECT,

    /** The collector cancels the reservation. */
    CANCEL,

    /** The reservation runs out, or an admin removes the product. */
    LAPSE,
}
