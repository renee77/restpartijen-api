package com.restpartijen.api.pricing.model

import kotlinx.serialization.Serializable

@Serializable
    data class MaintenanceReport(
        val lapsedReservations: Int,
        val expiredProducts: Int
    )