package com.restpartijen.api.product.model

import com.restpartijen.api.shared.DomainRuleException
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

/**
 * The opening and closing time of a supplier on one day of the week (US-10, §9.9).
 *
 * A closed day has no OpeningHours at all, so every instance describes an open day.
 */
data class OpeningHours(val day: DayOfWeek, val opensAt: LocalTime, val closesAt: LocalTime) {
    init {
        // A domain rule, so DomainRuleException (422) instead of require (IllegalArgumentException).
        // The message names the day, because a week has up to seven of these.
        if (closesAt <= opensAt) {
            throw DomainRuleException("closesAt must be after opensAt on $day", field = "closesAt")
        }
    }
}
