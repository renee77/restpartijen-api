package com.restpartijen.api.product

import com.restpartijen.api.product.model.OpeningHours
import com.restpartijen.api.product.service.OpeningHoursCommand
import com.restpartijen.api.product.service.OpeningHoursService
import com.restpartijen.api.product.service.OwnershipGuard
import com.restpartijen.api.shared.DomainRuleException
import com.restpartijen.api.shared.ForbiddenException
import com.restpartijen.api.shared.NotFoundException
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * The domain logic of US-10 (§5.10) against the fake SupplierRepository.
 * The fake knows aSupplier (id 1), owned by user 7. replaceOpeningHours takes commands: it builds OpeningHours
 * itself, after the 404 and 403 checks, so only the owner hears about the week rules.
 */
class OpeningHoursServiceTest {

    private val suppliers = fakeSupplierRepository(aSupplier, ownerUserId = 7)
    private val service = OpeningHoursService(suppliers, OwnershipGuard(suppliers))

    // --- getOpeningHours ---

    @Test
    fun `getOpeningHours returns the stored week of the supplier`() = runTest {
        // Arrange
        suppliers.updateOpeningHours(aSupplier.id, listOf(monday, saturday))

        // Act
        val week = service.getOpeningHours(aSupplier.id)

        // Assert
        assertEquals(listOf(monday, saturday), week)
    }

    @Test
    fun `getOpeningHours throws NotFoundException for an unknown supplier`() = runTest {
        // Act + Assert: an empty list would look like "always closed" instead of "does not exist"
        assertFailsWith<NotFoundException> {
            service.getOpeningHours(999)
        }
    }

    // --- replaceOpeningHours ---

    @Test
    fun `replaceOpeningHours stores the week and returns it from Monday to Sunday`() = runTest {
        // Act: out of order, so the result proves the stored week is returned, not the request
        val week = service.replaceOpeningHours(
            userId = 7, supplierId = aSupplier.id, week = listOf(saturday.asCommand(), monday.asCommand()),
        )

        // Assert
        assertEquals(listOf(monday, saturday), week)
        assertEquals(listOf(monday, saturday), suppliers.getOpeningHours(aSupplier.id))
    }

    @Test
    fun `replaceOpeningHours throws NotFoundException for an unknown supplier, before the ownership check`() =
        runTest {
            // Act + Assert: user 7 does not own supplier 999 either, so 404 proves the order of the checks
            assertFailsWith<NotFoundException> {
                service.replaceOpeningHours(userId = 7, supplierId = 999, week = listOf(monday.asCommand()))
            }
        }

    @Test
    fun `replaceOpeningHours refuses another user and keeps the old week`() = runTest {
        // Arrange
        suppliers.updateOpeningHours(aSupplier.id, listOf(saturday))

        // Act
        assertFailsWith<ForbiddenException> {
            service.replaceOpeningHours(userId = 8, supplierId = aSupplier.id, week = listOf(monday.asCommand()))
        }

        // Assert
        assertEquals(listOf(saturday), suppliers.getOpeningHours(aSupplier.id))
    }

    @Test
    fun `replaceOpeningHours checks ownership before the week, so another user never learns the rules`() = runTest {
        // Act + Assert: an empty week from another user is 403, not 422
        assertFailsWith<ForbiddenException> {
            service.replaceOpeningHours(userId = 8, supplierId = aSupplier.id, week = emptyList())
        }
    }

    @Test
    fun `replaceOpeningHours refuses an empty week and keeps the old week`() = runTest {
        // Arrange
        suppliers.updateOpeningHours(aSupplier.id, listOf(saturday))

        // Act: a supplier always has opening hours (decision 23 Oct 2026)
        val exception = assertFailsWith<DomainRuleException> {
            service.replaceOpeningHours(userId = 7, supplierId = aSupplier.id, week = emptyList())
        }

        // Assert
        assertEquals("openingHours", exception.field)
        assertEquals(listOf(saturday), suppliers.getOpeningHours(aSupplier.id))
    }

    @Test
    fun `replaceOpeningHours refuses a day that appears twice and keeps the old week`() = runTest {
        // Arrange
        suppliers.updateOpeningHours(aSupplier.id, listOf(saturday))
        val mondayLate = OpeningHoursCommand(DayOfWeek.MONDAY, opensAt = LocalTime(12, 0), closesAt = LocalTime(22, 0))

        // Act
        val exception = assertFailsWith<DomainRuleException> {
            val week = listOf(monday.asCommand(), mondayLate)
            service.replaceOpeningHours(userId = 7, supplierId = aSupplier.id, week = week)
        }

        // Assert: the message names the day, so the supplier knows which one to fix
        assertEquals("openingHours", exception.field)
        assertTrue("MONDAY" in exception.message.orEmpty())
        assertEquals(listOf(saturday), suppliers.getOpeningHours(aSupplier.id))
    }

    @Test
    fun `replaceOpeningHours refuses closing before opening and keeps the old week`() = runTest {
        // Arrange
        suppliers.updateOpeningHours(aSupplier.id, listOf(saturday))

        // Act: the check of OpeningHours itself, reached when the service builds the week
        val exception = assertFailsWith<DomainRuleException> {
            service.replaceOpeningHours(userId = 7, supplierId = aSupplier.id, week = listOf(closingBeforeOpening))
        }

        // Assert
        assertEquals("closesAt", exception.field)
        assertEquals(listOf(saturday), suppliers.getOpeningHours(aSupplier.id))
    }

    @Test
    fun `replaceOpeningHours checks the supplier before closing before opening`() = runTest {
        // Act + Assert: 404 for an unknown supplier and 403 for another user, not 422 about the week
        assertFailsWith<NotFoundException> {
            service.replaceOpeningHours(userId = 7, supplierId = 999, week = listOf(closingBeforeOpening))
        }
        assertFailsWith<ForbiddenException> {
            service.replaceOpeningHours(userId = 8, supplierId = aSupplier.id, week = listOf(closingBeforeOpening))
        }
    }

    private fun OpeningHours.asCommand() = OpeningHoursCommand(day, opensAt, closesAt)

    private val closingBeforeOpening =
        OpeningHoursCommand(DayOfWeek.MONDAY, opensAt = LocalTime(21, 0), closesAt = LocalTime(8, 0))

    private val monday = OpeningHours(DayOfWeek.MONDAY, opensAt = LocalTime(8, 0), closesAt = LocalTime(21, 0))
    private val saturday = OpeningHours(DayOfWeek.SATURDAY, opensAt = LocalTime(9, 0), closesAt = LocalTime(18, 0))
}
