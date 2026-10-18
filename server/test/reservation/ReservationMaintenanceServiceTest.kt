package com.restpartijen.api.reservation

import com.restpartijen.api.reservation.model.Reservation
import com.restpartijen.api.reservation.model.ReservationStatus
import com.restpartijen.api.reservation.service.ReservationMaintenanceService
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class ReservationMaintenanceServiceTest {

    private val now = Instant.parse("2026-10-20T12:00:00Z")

    private fun reservation(
        id: Long,
        productId: Long,
        expiresAt: Instant,
        reservedAt: Instant = expiresAt - 24.hours,
        status: ReservationStatus = ReservationStatus.ACTIVE,
    ) = Reservation(
        id, productId, collectorId = 7, reservedPrice = Money.fromCents(199),
        reservedAt = reservedAt, expiresAt = expiresAt, status = status,
    )

    private fun reservedProduct(id: Long, window: PickupWindow = PickupWindow(now - 2.days, now + 2.days)) =
        FakeProduct(id = id, status = ProductStatus.RESERVED, pickupWindow = window)

    @Test
    fun `a reservation older than 24 hours lapses even inside the pickup window`() = runTest {
        // Arrange: window still open for two days, but expiresAt (reservedAt + 24h) has passed
        val reservations = FakeReservationRepository(listOf(reservation(1, productId = 10, expiresAt = now - 1.minutes)))
        val updater = FakeProductStatusUpdater()
        val service = ReservationMaintenanceService(FakeProductReader(listOf(reservedProduct(10))), updater, reservations)

        // Act
        val lapsed = service.lapseOverdue(now)

        // Assert
        assertEquals(1, lapsed)
        assertEquals(ReservationStatus.LAPSED, reservations.findById(1)?.status)
        assertEquals(listOf(10L), updater.listed)
    }

    @Test
    fun `a reservation after the pickup window lapses`() = runTest {
        // Arrange: reserved 5 hours ago, window ended 1 hour ago, so expiresAt = window.until
        val window = PickupWindow(now - 6.hours, now - 1.hours)
        val reservations = FakeReservationRepository(
            listOf(reservation(1, productId = 10, expiresAt = window.until, reservedAt = now - 5.hours))
        )
        val updater = FakeProductStatusUpdater()
        val service = ReservationMaintenanceService(FakeProductReader(listOf(reservedProduct(10, window))), updater, reservations)

        // Act
        val lapsed = service.lapseOverdue(now)

        // Assert
        assertEquals(1, lapsed)
        assertEquals(ReservationStatus.LAPSED, reservations.findById(1)?.status)
        assertEquals(listOf(10L), updater.listed)
    }

    @Test
    fun `a reservation within both limits stays ACTIVE`() = runTest {
        // Arrange: still valid for another hour
        val reservations = FakeReservationRepository(listOf(reservation(1, productId = 10, expiresAt = now + 1.hours)))
        val updater = FakeProductStatusUpdater()
        val service = ReservationMaintenanceService(FakeProductReader(listOf(reservedProduct(10))), updater, reservations)

        // Act
        val lapsed = service.lapseOverdue(now)

        // Assert
        assertEquals(0, lapsed)
        assertEquals(ReservationStatus.ACTIVE, reservations.findById(1)?.status)
        assertTrue(updater.listed.isEmpty())
    }

    @Test
    fun `a reservation that expires exactly now lapses`() = runTest {
        // Arrange: the boundary; 24 hours old counts as overdue (<=, not <)
        val reservations = FakeReservationRepository(listOf(reservation(1, productId = 10, expiresAt = now)))
        val updater = FakeProductStatusUpdater()
        val service = ReservationMaintenanceService(FakeProductReader(listOf(reservedProduct(10))), updater, reservations)

        // Act
        val lapsed = service.lapseOverdue(now)

        // Assert
        assertEquals(1, lapsed)
        assertEquals(ReservationStatus.LAPSED, reservations.findById(1)?.status)
    }

    @Test
    fun `lapseActiveFor returns true and lapses the active reservation`() = runTest {
        // Arrange: not overdue; the admin lapses it anyway before removing the product (US-09)
        val reservations = FakeReservationRepository(listOf(reservation(1, productId = 10, expiresAt = now + 1.hours)))
        val updater = FakeProductStatusUpdater()
        val service = ReservationMaintenanceService(FakeProductReader(listOf(reservedProduct(10))), updater, reservations)

        // Act
        val result = service.lapseActiveFor(10)

        // Assert
        assertTrue(result)
        assertEquals(ReservationStatus.LAPSED, reservations.findById(1)?.status)
        assertEquals(listOf(10L), updater.listed)
    }

    @Test
    fun `lapseActiveFor returns false when the product has no active reservation`() = runTest {
        // Arrange: the product only has an old, cancelled reservation
        val reservations = FakeReservationRepository(
            listOf(reservation(1, productId = 10, expiresAt = now + 1.hours, status = ReservationStatus.CANCELLED))
        )
        val updater = FakeProductStatusUpdater()
        val service = ReservationMaintenanceService(FakeProductReader(listOf(reservedProduct(10))), updater, reservations)

        // Act
        val result = service.lapseActiveFor(10)

        // Assert
        assertFalse(result)
        assertEquals(ReservationStatus.CANCELLED, reservations.findById(1)?.status)
        assertTrue(updater.listed.isEmpty())
    }
}