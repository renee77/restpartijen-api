package com.restpartijen.api.product.service

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

/**
 * One open day as a supplier sends it (US-10), in domain types but not yet checked against the week rules.
 *
 * OpeningHoursService turns it into OpeningHours only after the 404 and 403 checks. OpeningHours checks
 * closing after opening itself, so without this step another supplier would hear about that rule (§5.10).
 *
 * @property day the day of the week
 * @property opensAt the local opening time
 * @property closesAt the local closing time; not checked against [opensAt] yet
 */
data class OpeningHoursCommand(val day: DayOfWeek, val opensAt: LocalTime, val closesAt: LocalTime)
