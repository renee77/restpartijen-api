package com.restpartijen.api.product.dto

import com.restpartijen.api.product.service.OpeningHoursCommand
import com.restpartijen.api.shared.ValidationException
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/**
 * One open day in the body of PUT /api/v1/suppliers/{id}/opening-hours (US-10, §5.10), for example
 * `{"day": "MONDAY", "opensAt": "08:00", "closesAt": "21:00"}`.
 *
 * Typed fields instead of strings, like LocalDate and Allergen in PlaceProductRequest: an unknown day
 * or a time that is not ISO 8601 cannot be read, so the JSON reader already gives a 400.
 *
 * @property day the day of the week, such as MONDAY
 * @property opensAt the local opening time, in whole minutes
 * @property closesAt the local closing time, in whole minutes
 */
@Serializable
data class OpeningHoursRequest(val day: DayOfWeek, val opensAt: LocalTime, val closesAt: LocalTime)

/**
 * Turns the request into a command; it only translates. The service builds OpeningHours from it after the
 * 404 and 403 checks, and OpeningHours then checks closing after opening itself (422).
 */
fun OpeningHoursRequest.toCommand() = OpeningHoursCommand(day, opensAt, closesAt)

/**
 * Checks the shape of one open day: opening hours are whole minutes (decision 23 Oct 2026). Seconds would
 * also be cut off by the time column, so a stored week would differ from the one that was sent.
 *
 * @throws ValidationException when opensAt or closesAt has seconds; StatusPages turns it into a 400.
 */
fun requireWholeMinutes(request: OpeningHoursRequest) {
    requireWholeMinute(request.opensAt, field = "opensAt")
    requireWholeMinute(request.closesAt, field = "closesAt")
}

/** Refuses [time] when it has seconds or nanoseconds, with a ValidationException (400) that names [field]. */
private fun requireWholeMinute(time: LocalTime, field: String) {
    if (time.second != 0 || time.nanosecond != 0) {
        throw ValidationException("$field must be whole minutes, for example \"08:00\"", field = field)
    }
}
