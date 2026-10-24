package com.restpartijen.api.product

import com.restpartijen.api.product.model.OpeningHours
import com.restpartijen.api.shared.DomainRuleException
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OpeningHoursTest {

    // --- closing time after opening time (§5.10) ---

    @Test
    fun `accepts a closing time after the opening time`() {
        // Act
        val hours = OpeningHours(DayOfWeek.MONDAY, opensAt = LocalTime(8, 0), closesAt = LocalTime(21, 0))

        // Assert
        assertEquals(LocalTime(21, 0), hours.closesAt)
    }

    @Test
    fun `rejects a closing time before the opening time`() {
        // Act
        val exception = assertFailsWith<DomainRuleException> {
            OpeningHours(DayOfWeek.MONDAY, opensAt = LocalTime(18, 0), closesAt = LocalTime(9, 0))
        }

        // Assert: the field points at the wrong value, the message names the day it belongs to
        assertEquals("closesAt", exception.field)
        assertTrue("MONDAY" in exception.message.orEmpty())
    }

    @Test
    fun `rejects a closing time equal to the opening time`() {
        // Act + Assert: "not after" includes equal, so a day of zero minutes is refused
        assertFailsWith<DomainRuleException> {
            OpeningHours(DayOfWeek.SATURDAY, opensAt = LocalTime(9, 0), closesAt = LocalTime(9, 0))
        }
    }
}
