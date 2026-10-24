package com.restpartijen.api.product.service

import com.restpartijen.api.product.model.OpeningHours
import com.restpartijen.api.product.model.PLATFORM_TIME_ZONE
import com.restpartijen.api.shared.PickupWindow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * The outcome of PickupWindowCalculator: a window, or the reason there is none.
 *
 * Sealed, so a `when` on it must handle every case; the service turns each reason into its own 422.
 */
sealed interface PickupWindowResult {
    /**
     * The supplier is open at some moment between now and bestBeforeAt.
     *
     * @property window from the first open moment to the last closing moment before bestBeforeAt
     */
    data class Found(val window: PickupWindow) : PickupWindowResult

    /** bestBeforeAt is not after now, so there is no time left at all. */
    data object Expired : PickupWindowResult

    /** There is time left, but the supplier is not open at any moment of it. */
    data object NeverOpen : PickupWindowResult
}

/**
 * Derives the pickup window of a product from the opening hours of its supplier (US-10, §5.10, §9.9).
 *
 * A pure function: the same opening hours, moment and best-before date always give the same result.
 * Opening hours are local times in Europe/Amsterdam, so summer and winter time are taken into account.
 */
object PickupWindowCalculator {

    /**
     * The window between [now] and [bestBeforeAt] in which the supplier is open.
     *
     * It starts at [now] when the supplier is open then, otherwise at the next opening moment.
     * It ends at [bestBeforeAt] when the supplier is open then, otherwise at the last closing moment before it.
     *
     * @param openingHours the week of the supplier; a day that is not in the list is closed
     * @return [PickupWindowResult.Found] with the window; [PickupWindowResult.Expired] when [bestBeforeAt] is not
     *   after [now], checked first; otherwise [PickupWindowResult.NeverOpen] when the supplier is not open at any
     *   moment between [now] and [bestBeforeAt]
     */
    fun calculate(openingHours: List<OpeningHours>, now: Instant, bestBeforeAt: Instant): PickupWindowResult {
        // Step 1: no time left; equal counts too, like SurplusProduct.isExpired.
        if (bestBeforeAt <= now) return PickupWindowResult.Expired

        val local = now.toLocalDateTime(PLATFORM_TIME_ZONE)

        // Step 2: the first open moment from now on; too late when it is not before bestBeforeAt.
        val from = firstOpenMoment(openingHours, now, local)
        if (from == null || from >= bestBeforeAt) return PickupWindowResult.NeverOpen

        // Step 3: the last closing moment before bestBeforeAt.
        val until = lastClosingMoment(openingHours, bestBeforeAt)

        return PickupWindowResult.Found(PickupWindow(from, until))
    }

    /**
     * The first moment from [now] on at which the supplier is open; null when the week has no open day.
     * [local] is [now] in Amsterdam.
     *
     * Today is checked first; after that the next seven days, which covers every day of the week once.
     */
    private fun firstOpenMoment(openingHours: List<OpeningHours>, now: Instant, local: LocalDateTime): Instant? {
        // The opening hours of today; null when the supplier is closed today.
        val today = openingHours.find { it.day == local.date.dayOfWeek }

        // Open now: start now. Not open yet: start at opening time. Closing time itself is not open.
        if (today != null && local.time < today.closesAt) {
            return if (local.time < today.opensAt) atLocalTime(local.date, today.opensAt) else now
        }

        // Closed today, or already closed: the opening time of the next day that has opening hours.
        for (daysAhead in 1..7) {
            val date = local.date.plus(daysAhead, DateTimeUnit.DAY)
            val hours = openingHours.find { it.day == date.dayOfWeek } ?: continue
            return atLocalTime(date, hours.opensAt)
        }
        return null
    }

    /**
     * The last moment up to [bestBeforeAt] at which the supplier is still open.
     *
     * The day of [bestBeforeAt] is checked first; after that the seven days before it, which covers every day once.
     * Only called after step 2 found an open moment, so the week has an open day and there is always an answer.
     */
    private fun lastClosingMoment(openingHours: List<OpeningHours>, bestBeforeAt: Instant): Instant {
        val local = bestBeforeAt.toLocalDateTime(PLATFORM_TIME_ZONE)
        val lastDay = openingHours.find { it.day == local.date.dayOfWeek }

        // Open at bestBeforeAt: end at bestBeforeAt. Already closed: end at closing time.
        // Exactly at opening time there is no open moment left on that day, so it counts as closed.
        if (lastDay != null && local.time > lastDay.opensAt) {
            return if (local.time < lastDay.closesAt) bestBeforeAt else atLocalTime(local.date, lastDay.closesAt)
        }

        // Closed on that day, or not open yet: the closing time of the last day before it that has opening hours.
        for (daysBack in 1..7) {
            val date = local.date.minus(daysBack, DateTimeUnit.DAY)
            val hours = openingHours.find { it.day == date.dayOfWeek } ?: continue
            return atLocalTime(date, hours.closesAt)
        }
        // Unreachable when step 2 ran first; reaching it is a bug, so a 500 instead of a 422 for the supplier.
        error("No closing moment before $bestBeforeAt, although an open moment was found")
    }

    /** A local time on a date becomes an instant here, so summer and winter time are handled. */
    private fun atLocalTime(date: LocalDate, time: LocalTime): Instant =
        LocalDateTime(date, time).toInstant(PLATFORM_TIME_ZONE)
}
