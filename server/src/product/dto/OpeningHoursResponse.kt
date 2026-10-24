package com.restpartijen.api.product.dto

import com.restpartijen.api.product.model.OpeningHours
import kotlinx.serialization.Serializable

/**
 * JSON for one open day of a supplier (US-10, §5.10), for example
 * `{"day": "MONDAY", "opensAt": "08:00", "closesAt": "21:00"}`.
 *
 * Plain strings, like category and status in ProductResponse, so the JSON does not depend on how
 * kotlinx.datetime serializes its types. LocalTime.toString gives ISO 8601, "08:00" without seconds.
 */
@Serializable
data class OpeningHoursResponse(val day: String, val opensAt: String, val closesAt: String)

/** Maps one open day to the API response; it only translates. */
fun OpeningHours.toResponse() = OpeningHoursResponse(day.name, opensAt.toString(), closesAt.toString())
