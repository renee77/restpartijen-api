package com.restpartijen.api.shared

/**
 * What a search result shows about the supplier; the app computes the distance (§18.1).
 *
 * @property id the id of the supplier
 * @property name the company name, as shown in search results
 * @property latitude the latitude of the supplier's location
 * @property longitude the longitude of the supplier's location
 */
data class SupplierSummary(val id: Long, val name: String, val latitude: Double, val longitude: Double)
