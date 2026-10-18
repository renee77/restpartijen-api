package com.restpartijen.api.reservation

import com.restpartijen.api.reservation.model.Reservation
import com.restpartijen.api.reservation.model.ReservationStatus
import com.restpartijen.api.reservation.repository.ExposedReservationRepository
import com.restpartijen.api.reservation.repository.ReservationsTable
import com.restpartijen.api.shared.Money
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/** Integration test: the real repository against a fresh, empty H2 database per test. */
class ExposedReservationRepositoryTest {

    // Whole seconds: the database may store fewer digits than Instant has, and equality would fail.
    private val now = Instant.parse("2026-10-20T12:00:00Z")

    private fun reservation(productId: Long, expiresAt: Instant, status: ReservationStatus = ReservationStatus.ACTIVE) =
        Reservation(
            id = 0, productId = productId, collectorId = 7, reservedPrice = Money.fromCents(199),
            reservedAt = expiresAt - 24.hours, expiresAt = expiresAt, status = status
        )

    @Test
    fun `create stores the reservation and findById returns it`() = runTest {
        // Arrange
        createEmptyTestDatabase(ReservationsTable)
        val repository = ExposedReservationRepository()

        // Act
        val stored = repository.create(reservation(productId = 10, expiresAt = now + 1.hours))

        // Assert: id 0 is never generated, so a different id proves the database chose it
        assertNotEquals(0L, stored.id)
        assertEquals(stored, repository.findById(stored.id))
    }

    @Test
    fun `findOverdue returns only active reservations at or before now`() = runTest {
        // Arrange
        createEmptyTestDatabase(ReservationsTable)
        val repository = ExposedReservationRepository()
        val firstRow = repository.create(reservation(productId = 1, expiresAt = now))
        repository.create(reservation(productId = 2, expiresAt = now + 1.hours))
        repository.create(reservation(productId = 3, expiresAt = now - 1.hours, status = ReservationStatus.LAPSED))

        // Act
        val overdue = repository.findOverdue(now)

        // Assert
        assertEquals(listOf(firstRow), overdue)
    }

    @Test
    fun `findActiveByProductId ignores a cancelled reservation`() = runTest {
        // Arrange: same product, so only the status can tell the two apart
        createEmptyTestDatabase(ReservationsTable)
        val repository = ExposedReservationRepository()
        repository.create(reservation(productId = 5, expiresAt = now, status = ReservationStatus.CANCELLED))
        val active = repository.create(reservation(productId = 5, expiresAt = now + 1.hours))

        // Act
        val found = repository.findActiveByProductId(5)

        // Assert
        assertEquals(active, found)
    }

    @Test
    fun `update changes the status and returns true`() = runTest {
        // Arrange
        createEmptyTestDatabase(ReservationsTable)
        val repository = ExposedReservationRepository()
        val stored = repository.create(reservation(productId = 10, expiresAt = now))

        // Act
        val updated = repository.update(stored.copy(status = ReservationStatus.LAPSED))

        // Assert: read it back, so we know the database changed and not only the copy
        assertTrue(updated)
        assertEquals(stored.copy(status = ReservationStatus.LAPSED), repository.findById(stored.id))
    }

    @Test
    fun `update returns false for an unknown id`() = runTest {
        // Arrange: empty table, so no id exists
        createEmptyTestDatabase(ReservationsTable)
        val repository = ExposedReservationRepository()

        // Act
        val updated = repository.update(reservation(productId = 10, expiresAt = now).copy(id = 999))

        // Assert
        assertFalse(updated)
    }
}