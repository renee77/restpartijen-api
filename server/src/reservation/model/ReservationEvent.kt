package com.restpartijen.api.reservation.model

/**
 * What can happen to a reservation (§9.5).
 *
 * - RESERVE: a collector reserves a listed product.
 * - COLLECT: the collector picks the product up.
 * - CANCEL: the collector cancels the reservation.
 * - LAPSE: the reservation runs out, or an admin removes the product.
 */
enum class ReservationEvent { RESERVE, COLLECT, CANCEL, LAPSE }
