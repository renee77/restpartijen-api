package com.restpartijen.api.shared

/** What a search result shows about the supplier; the app computes the distance (§18.1). */
data class SupplierSummary(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double
)