package com.restpartijen.api.pricing.model

import kotlinx.serialization.Serializable

/**
 * Result of one maintenance run: how many state changes each step made.
 *
 * @property lapsedReservations number of active reservations that lapsed because
 * their pickup window ended or they were older than 24 hours.
 * @property expiredProducts number of listed products whose best-before moment
 * had passed and that were marked as expired.
 */

@Serializable
data class MaintenanceReport(val lapsedReservations: Int, val expiredProducts: Int)
