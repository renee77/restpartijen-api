package com.restpartijen.api.product

import com.restpartijen.api.product.model.OpeningHours
import com.restpartijen.api.product.service.PickupWindowCalculator
import com.restpartijen.api.product.service.PickupWindowResult
import com.restpartijen.api.shared.PickupWindow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

/**
 * The derived pickup window of US-10 (§5.10, §9.9).
 *
 * Every instant is written in UTC, with the local time in Amsterdam next to it. The expected values are worked
 * out by hand, so they do not depend on the time zone code under test. Until Sunday 25 October 2026 03:00
 * Amsterdam is on summer time (UTC+2), after that on winter time (UTC+1).
 * Fri 23 Oct, Sat 24 Oct, Sun 25 Oct (closed), Mon 26 Oct.
 */
class PickupWindowCalculatorTest {

    // --- the start of the window ---

    @Test
    fun `starts now and ends at bestBeforeAt when the supplier is open at both moments`() {
        // Act: the example of §9.9, a shop open until 18:00 and a product that expires at 15:00
        val result = PickupWindowCalculator.calculate(
            week,
            now = Instant.parse("2026-10-24T08:00:00Z"), // Sat 10:00
            bestBeforeAt = Instant.parse("2026-10-24T13:00:00Z"), // Sat 15:00
        )

        // Assert
        assertEquals(
            PickupWindowResult.Found(
                PickupWindow(
                    from = Instant.parse("2026-10-24T08:00:00Z"),
                    until = Instant.parse("2026-10-24T13:00:00Z"),
                ),
            ),
            result,
        )
    }

    @Test
    fun `starts at the next opening moment when the supplier is closed now`() {
        // Act
        val result = PickupWindowCalculator.calculate(
            week,
            now = Instant.parse("2026-10-23T04:00:00Z"), // Fri 06:00
            bestBeforeAt = Instant.parse("2026-10-23T13:00:00Z"), // Fri 15:00
        )

        // Assert
        val window = assertIs<PickupWindowResult.Found>(result).window
        assertEquals(Instant.parse("2026-10-23T06:00:00Z"), window.from) // Fri 08:00
    }

    @Test
    fun `treats the closing time as closed, so the window starts the next day`() {
        // Act: open means opensAt up to, but not including, closesAt
        val result = PickupWindowCalculator.calculate(
            week,
            now = Instant.parse("2026-10-23T19:00:00Z"), // Fri 21:00
            bestBeforeAt = Instant.parse("2026-10-24T10:00:00Z"), // Sat 12:00
        )

        // Assert
        val window = assertIs<PickupWindowResult.Found>(result).window
        assertEquals(Instant.parse("2026-10-24T07:00:00Z"), window.from) // Sat 09:00
    }

    @Test
    fun `skips a closed day and starts on winter time after the clocks go back`() {
        // Act: Sunday has no opening hours; Monday 08:00 is 07:00 UTC on winter time, not 06:00
        val result = PickupWindowCalculator.calculate(
            week,
            now = Instant.parse("2026-10-24T17:00:00Z"), // Sat 19:00, after closing
            bestBeforeAt = Instant.parse("2026-10-26T11:00:00Z"), // Mon 12:00
        )

        // Assert
        val window = assertIs<PickupWindowResult.Found>(result).window
        assertEquals(Instant.parse("2026-10-26T07:00:00Z"), window.from) // Mon 08:00
    }

    // --- the end of the window ---

    @Test
    fun `ends at the last closing moment when the supplier is closed at bestBeforeAt`() {
        // Act
        val result = PickupWindowCalculator.calculate(
            week,
            now = Instant.parse("2026-10-23T08:00:00Z"), // Fri 10:00
            bestBeforeAt = Instant.parse("2026-10-24T18:00:00Z"), // Sat 20:00
        )

        // Assert
        val window = assertIs<PickupWindowResult.Found>(result).window
        assertEquals(Instant.parse("2026-10-24T16:00:00Z"), window.until) // Sat 18:00
    }

    @Test
    fun `ends at the closing moment on winter time after the clocks go back`() {
        // Act: Monday 21:00 is 20:00 UTC on winter time, not 19:00
        val result = PickupWindowCalculator.calculate(
            week,
            now = Instant.parse("2026-10-24T08:00:00Z"), // Sat 10:00
            bestBeforeAt = Instant.parse("2026-10-26T21:00:00Z"), // Mon 22:00
        )

        // Assert
        val window = assertIs<PickupWindowResult.Found>(result).window
        assertEquals(Instant.parse("2026-10-26T20:00:00Z"), window.until) // Mon 21:00
    }

    @Test
    fun `ends at the closing time of the day before when bestBeforeAt is exactly the opening time`() {
        // Act: at opening time itself there is no open moment left on Monday
        val result = PickupWindowCalculator.calculate(
            week,
            now = Instant.parse("2026-10-24T08:00:00Z"), // Sat 10:00
            bestBeforeAt = Instant.parse("2026-10-26T07:00:00Z"), // Mon 08:00
        )

        // Assert
        val window = assertIs<PickupWindowResult.Found>(result).window
        assertEquals(Instant.parse("2026-10-24T16:00:00Z"), window.until) // Sat 18:00
    }

    // --- a supplier open one day a week: the search goes a full week ahead and back ---

    @Test
    fun `finds the opening a full week ahead`() {
        // Act
        val result = PickupWindowCalculator.calculate(
            saturdayOnly,
            now = Instant.parse("2026-10-24T17:00:00Z"), // Sat 19:00, after closing
            bestBeforeAt = Instant.parse("2026-10-31T13:00:00Z"), // Sat 31 Oct 14:00
        )

        // Assert
        val window = assertIs<PickupWindowResult.Found>(result).window
        assertEquals(Instant.parse("2026-10-31T08:00:00Z"), window.from) // Sat 31 Oct 09:00
    }

    @Test
    fun `finds the closing a full week back`() {
        // Act
        val result = PickupWindowCalculator.calculate(
            saturdayOnly,
            now = Instant.parse("2026-10-24T08:00:00Z"), // Sat 10:00
            bestBeforeAt = Instant.parse("2026-10-31T07:00:00Z"), // Sat 31 Oct 08:00, not open yet
        )

        // Assert
        val window = assertIs<PickupWindowResult.Found>(result).window
        assertEquals(Instant.parse("2026-10-24T16:00:00Z"), window.until) // Sat 24 Oct 18:00
    }

    // --- no window (§5.10: placing is refused with 422) ---

    @Test
    fun `returns NeverOpen when the supplier is not open between now and bestBeforeAt`() {
        // Act
        val result = PickupWindowCalculator.calculate(
            week,
            now = Instant.parse("2026-10-24T17:00:00Z"), // Sat 19:00, after closing
            bestBeforeAt = Instant.parse("2026-10-25T19:00:00Z"), // Sun 20:00, closed all day
        )

        // Assert
        assertEquals(PickupWindowResult.NeverOpen, result)
    }

    @Test
    fun `returns NeverOpen when bestBeforeAt is exactly the next opening moment`() {
        // Act: the window would last zero minutes, and a PickupWindow must start before it ends
        val result = PickupWindowCalculator.calculate(
            week,
            now = Instant.parse("2026-10-24T17:00:00Z"), // Sat 19:00, after closing
            bestBeforeAt = Instant.parse("2026-10-26T07:00:00Z"), // Mon 08:00
        )

        // Assert
        assertEquals(PickupWindowResult.NeverOpen, result)
    }

    @Test
    fun `returns NeverOpen for a supplier without opening hours`() {
        // Act: the service refuses an empty week, but seed data or old rows may still lack one
        val result = PickupWindowCalculator.calculate(
            emptyList(),
            now = Instant.parse("2026-10-23T08:00:00Z"), // Fri 10:00
            bestBeforeAt = Instant.parse("2026-10-24T13:00:00Z"), // Sat 15:00
        )

        // Assert
        assertEquals(PickupWindowResult.NeverOpen, result)
    }

    @Test
    fun `returns Expired when bestBeforeAt is exactly now`() {
        // Act: the supplier is open, but there is no time left; equal counts as expired, like isExpired
        val result = PickupWindowCalculator.calculate(
            week,
            now = Instant.parse("2026-10-23T08:00:00Z"), // Fri 10:00
            bestBeforeAt = Instant.parse("2026-10-23T08:00:00Z"),
        )

        // Assert
        assertEquals(PickupWindowResult.Expired, result)
    }

    @Test
    fun `returns Expired when bestBeforeAt is in the past`() {
        // Act
        val result = PickupWindowCalculator.calculate(
            week,
            now = Instant.parse("2026-10-23T08:00:00Z"), // Fri 10:00
            bestBeforeAt = Instant.parse("2026-10-22T08:00:00Z"), // Thu 10:00
        )

        // Assert
        assertEquals(PickupWindowResult.Expired, result)
    }

    @Test
    fun `checks Expired before NeverOpen, so the supplier hears the real reason`() {
        // Act: both apply; a date in the past is the cause, and opening hours would not fix it
        val result = PickupWindowCalculator.calculate(
            emptyList(),
            now = Instant.parse("2026-10-23T08:00:00Z"), // Fri 10:00
            bestBeforeAt = Instant.parse("2026-10-22T08:00:00Z"), // Thu 10:00
        )

        // Assert
        assertEquals(PickupWindowResult.Expired, result)
    }

    // Only Sat 09:00-18:00.
    private val saturdayOnly =
        listOf(OpeningHours(DayOfWeek.SATURDAY, opensAt = LocalTime(9, 0), closesAt = LocalTime(18, 0)))

    // Mon to Fri 08:00-21:00, Sat 09:00-18:00, Sun closed.
    private val week =
        listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
            .map { OpeningHours(it, opensAt = LocalTime(8, 0), closesAt = LocalTime(21, 0)) } +
            OpeningHours(DayOfWeek.SATURDAY, opensAt = LocalTime(9, 0), closesAt = LocalTime(18, 0))
}
