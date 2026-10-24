package com.restpartijen.api.product

import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.product.model.OpeningHours
import com.restpartijen.api.product.repository.ExposedSupplierRepository
import com.restpartijen.api.product.repository.SupplierOpeningHoursTable
import com.restpartijen.api.shared.Role
import com.restpartijen.api.shared.SupplierSummary
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import com.restpartijen.api.testsupport.insertSupplier
import com.restpartijen.api.testsupport.insertUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration test: the real SupplierIdLookup of F1 against a real, empty H2 database per test (A15).
 * One test per promise in the KDoc of SupplierIdLookup, plus the opening hours of US-10.
 */
class ExposedSupplierRepositoryTest {

    private val repository = ExposedSupplierRepository()

    @Test
    fun `lookupSupplierId returns the id of the supplier of that user`() = runTest {
        // Arrange: a collector first, so the user id (2) differs from the supplier id (1).
        // With equal ids, a query on the wrong column would still give the right answer.
        createEmptyTestDatabase(UsersTable, SuppliersTable)
        insertUser(Role.COLLECTOR)
        val userId = insertUser(Role.SUPPLIER)
        val supplierId = insertSupplier(userId)

        // Act
        val found = repository.lookupSupplierId(userId)

        // Assert
        assertEquals(supplierId, found)
    }

    @Test
    fun `lookupSupplierId returns null for a user without a supplier`() = runTest {
        // Arrange: the collector (user 1) has no supplier row, but supplier 1 exists for another user,
        // so a query on the supplier id instead of the user id would wrongly find it.
        createEmptyTestDatabase(UsersTable, SuppliersTable)
        val userId = insertUser(Role.COLLECTOR)
        insertSupplier(insertUser(Role.SUPPLIER))

        // Act
        val found = repository.lookupSupplierId(userId)

        // Assert
        assertNull(found)
    }

    @Test
    fun `lookupSupplierId returns null for an unknown user id`() = runTest {
        // Arrange
        createEmptyTestDatabase(UsersTable, SuppliersTable)

        // Act
        val found = repository.lookupSupplierId(999)

        // Assert
        assertNull(found)
    }

    @Test
    fun `findByUserId returns the supplier of that user as a summary`() = runTest {
        // Arrange: a collector first, so the user id (2) differs from the supplier id (1)
        createEmptyTestDatabase(UsersTable, SuppliersTable)
        insertUser(Role.COLLECTOR)
        val userId = insertUser(Role.SUPPLIER)
        val supplierId = insertSupplier(userId)

        // Act
        val found = repository.findByUserId(userId)

        // Assert: the values insertSupplier stores
        assertEquals(SupplierSummary(id = supplierId, name = "Bakkerij Test", latitude = 51.59, longitude = 4.78), found)
    }

    @Test
    fun `findByUserId returns null for a user without a supplier`() = runTest {
        // Arrange: supplier 1 exists for another user, so the wrong column would wrongly find it
        createEmptyTestDatabase(UsersTable, SuppliersTable)
        val userId = insertUser(Role.COLLECTOR)
        insertSupplier(insertUser(Role.SUPPLIER))

        // Act
        val found = repository.findByUserId(userId)

        // Assert
        assertNull(found)
    }

    @Test
    fun `exists returns true for the id of a stored supplier`() = runTest {
        // Arrange: a collector first, so the supplier id (1) differs from its user id (2).
        // User 1 has no supplier, so a query on the user id column would wrongly give false.
        createEmptyTestDatabase(UsersTable, SuppliersTable)
        insertUser(Role.COLLECTOR)
        val supplierId = insertSupplier(insertUser(Role.SUPPLIER))

        // Act
        val found = repository.exists(supplierId)

        // Assert
        assertTrue(found)
    }

    @Test
    fun `exists returns false for an unknown supplier id`() = runTest {
        // Arrange: supplier 1 belongs to user 2, so a query on the user id column would wrongly find id 2
        createEmptyTestDatabase(UsersTable, SuppliersTable)
        insertUser(Role.COLLECTOR)
        val supplierId = insertSupplier(insertUser(Role.SUPPLIER))

        // Act
        val found = repository.exists(supplierId + 1)

        // Assert
        assertFalse(found)
    }

    // --- opening hours (US-10) ---

    @Test
    fun `updateOpeningHours stores the week and getOpeningHours returns it from Monday to Sunday`() = runTest {
        // Arrange: stored out of order, so the result proves the repository sorts by day
        val supplierId = emptyDatabaseWithSupplierAndOpeningHours()
        val week = listOf(saturday, monday)

        // Act
        repository.updateOpeningHours(supplierId, week)

        // Assert
        assertEquals(listOf(monday, saturday), repository.getOpeningHours(supplierId))
    }

    @Test
    fun `getOpeningHours returns an empty list for a supplier without opening hours`() = runTest {
        // Arrange
        val supplierId = emptyDatabaseWithSupplierAndOpeningHours()

        // Act
        val found = repository.getOpeningHours(supplierId)

        // Assert
        assertEquals(emptyList(), found)
    }

    @Test
    fun `updateOpeningHours replaces the whole week, so a day that is left out is closed`() = runTest {
        // Arrange
        val supplierId = emptyDatabaseWithSupplierAndOpeningHours()
        repository.updateOpeningHours(supplierId, listOf(monday, saturday))

        // Act: Saturday is no longer in the week
        repository.updateOpeningHours(supplierId, listOf(mondayLate))

        // Assert
        assertEquals(listOf(mondayLate), repository.getOpeningHours(supplierId))
    }

    @Test
    fun `updateOpeningHours leaves the opening hours of another supplier alone`() = runTest {
        // Arrange
        val supplierId = emptyDatabaseWithSupplierAndOpeningHours()
        val otherSupplierId = insertSupplier(insertUser(Role.SUPPLIER))
        repository.updateOpeningHours(otherSupplierId, listOf(saturday))

        // Act
        repository.updateOpeningHours(supplierId, listOf(monday))

        // Assert: replacing one week must not delete the rows of every supplier
        assertEquals(listOf(saturday), repository.getOpeningHours(otherSupplierId))
        assertEquals(listOf(monday), repository.getOpeningHours(supplierId))
    }

    @Test
    fun `updateOpeningHours keeps the old week when the new week cannot be stored`() = runTest {
        // Arrange
        val supplierId = emptyDatabaseWithSupplierAndOpeningHours()
        repository.updateOpeningHours(supplierId, listOf(saturday))

        // Act: two Mondays break the primary key (supplier_id, day_of_week) halfway through
        assertFails { repository.updateOpeningHours(supplierId, listOf(monday, mondayLate)) }

        // Assert: delete and insert run in one transaction, so the delete is rolled back too
        assertEquals(listOf(saturday), repository.getOpeningHours(supplierId))
    }

    @Test
    fun `two simultaneous replacements of one week both succeed and leave one of the two weeks`() = runTest {
        // Arrange: a supplier without opening hours, so both transactions find nothing to delete and both insert
        // MONDAY. Without the lock one of them breaks the primary key (a 500); the default retry of Exposed hid
        // that, which a run with defaultMaxAttempts = 1 showed (23 Oct 2026).
        val supplierId = emptyDatabaseWithSupplierAndOpeningHours()
        val weeks = listOf(listOf(monday), listOf(mondayLate, saturday))

        // Act: on real threads at the same time, repeated, because a race does not show every time
        repeat(20) {
            repository.updateOpeningHours(supplierId, emptyList())
            val results = coroutineScope {
                weeks.map { week ->
                    async(Dispatchers.IO) { runCatching { repository.updateOpeningHours(supplierId, week) } }
                }.awaitAll()
            }

            // Assert: no failure, and the stored week is exactly one of the two, never a mix
            assertTrue(results.all { it.isSuccess }, "results: $results")
            assertTrue(repository.getOpeningHours(supplierId) in weeks)
        }
    }

    private val monday = OpeningHours(DayOfWeek.MONDAY, opensAt = LocalTime(8, 0), closesAt = LocalTime(21, 0))
    private val mondayLate = OpeningHours(DayOfWeek.MONDAY, opensAt = LocalTime(12, 0), closesAt = LocalTime(22, 0))
    private val saturday = OpeningHours(DayOfWeek.SATURDAY, opensAt = LocalTime(9, 0), closesAt = LocalTime(18, 0))

    // An empty database with the opening-hours table and one supplier; returns the supplier id.
    private fun emptyDatabaseWithSupplierAndOpeningHours(): Long {
        createEmptyTestDatabase(UsersTable, SuppliersTable, SupplierOpeningHoursTable)
        return insertSupplier(insertUser(Role.SUPPLIER))
    }
}
