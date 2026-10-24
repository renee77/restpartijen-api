package com.restpartijen.api.product.dto

import com.restpartijen.api.product.model.PLATFORM_TIME_ZONE
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlin.time.Instant

/**
 * The moment a best-before date ends: the start of the next day in Amsterdam (decision 23 Oct 2026).
 *
 * A label shows a date, not a time, so the product is good for the whole day. The start of the next day is
 * an exact end point that does not count itself, like closesAt; 23:59 would lose the last minute.
 * Shared by POST and PUT, so both requests use the same rule.
 */
fun LocalDate.toBestBeforeAt(): Instant = plus(1, DateTimeUnit.DAY).atStartOfDayIn(PLATFORM_TIME_ZONE)
