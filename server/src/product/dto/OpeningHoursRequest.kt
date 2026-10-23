package com.restpartijen.api.product.dto

import com.restpartijen.api.product.model.OpeningHours
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/**
 * One open day in the body of PUT /api/v1/suppliers/{id}/opening-hours (US-10, §5.10), for example
 * `{"day": "MONDAY", "opensAt": "08:00", "closesAt": "21:00"}`.
 *
 * Typed fields instead of strings, like Instant and Allergen in PlaceProductRequest: an unknown day
 * or a time that is not ISO 8601 cannot be read, so the JSON reader already gives a 400.
 */
@Serializable
data class OpeningHoursRequest(
    val day: DayOfWeek,
    val opensAt: LocalTime,
    val closesAt: LocalTime,
)

/** Turns the request into the domain model; OpeningHours then checks closing after opening itself (422). */
fun OpeningHoursRequest.toOpeningHours() = OpeningHours(day, opensAt, closesAt)
