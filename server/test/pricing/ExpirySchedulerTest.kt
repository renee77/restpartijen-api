package com.restpartijen.api.pricing

import com.restpartijen.api.pricing.model.MaintenanceReport
import com.restpartijen.api.pricing.service.ExpiryScheduler
import com.restpartijen.api.security.TestClock
import com.restpartijen.api.shared.ProductStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class ExpirySchedulerTest {

    // Counts how often the clock is asked for the time
    private class CountingClock : Clock {
        var calls = 0
        override fun now(): Instant {
            calls++
            return Instant.parse("2026-09-26T12:00:00Z")
        }
    }

    @Test
    fun `an expired product is expired and counted`() = runTest {
        // Arrange
        val updater = FakeProductStatusUpdater()
        val scheduler = ExpiryScheduler(
            FakeReservationMaintenance(),
            FakeProductReader(expiredListings = listOf(FakeProductView(1))),
            updater,
            TestClock.fixed,
        )

        // Act
        val report = scheduler.runMaintenance()

        // Assert
        assertEquals(listOf(1L), updater.expiredIds)
        assertEquals(MaintenanceReport(lapsedReservations = 0, expiredProducts = 1), report)
    }

    @Test
    fun `a collected product is left alone`() = runTest {
        // Arrange: the reader knows a COLLECTED product, but it is not an expired listing
        val updater = FakeProductStatusUpdater()
        val scheduler = ExpiryScheduler(
            FakeReservationMaintenance(),
            FakeProductReader(products = listOf(FakeProductView(1, ProductStatus.COLLECTED))),
            updater,
            TestClock.fixed,
        )

        // Act
        val report = scheduler.runMaintenance()

        // Assert
        assertTrue(updater.expiredIds.isEmpty())
        assertEquals(0, report.expiredProducts)
    }

    @Test
    fun `reservations lapse before products expire`() = runTest {
        // Arrange: one shared call list across both fakes shows the order
        val calls = mutableListOf<String>()
        val scheduler = ExpiryScheduler(
            FakeReservationMaintenance(calls = calls),
            FakeProductReader(expiredListings = listOf(FakeProductView(1))),
            FakeProductStatusUpdater(calls = calls),
            TestClock.fixed,
        )

        // Act
        scheduler.runMaintenance()

        // Assert
        assertEquals(listOf("lapseOverdue", "markExpired"), calls)
    }

    @Test
    fun `both steps use the same moment`() = runTest {
        // Arrange
        val clock = CountingClock()
        val scheduler = ExpiryScheduler(
            FakeReservationMaintenance(),
            FakeProductReader(),
            FakeProductStatusUpdater(),
            clock,
        )

        // Act
        scheduler.runMaintenance()

        // Assert: the clock was asked once, and that moment went to both steps
        assertEquals(1, clock.calls)
    }

    @Test
    fun `the report shows how many reservations lapsed`() = runTest {
        // Arrange
        val scheduler = ExpiryScheduler(
            FakeReservationMaintenance(lapsedCount = 2),
            FakeProductReader(),
            FakeProductStatusUpdater(),
            TestClock.fixed,
        )

        // Act
        val report = scheduler.runMaintenance()

        // Assert
        assertEquals(2, report.lapsedReservations)
    }

    @Test
    fun `an updater that changes fewer products than found is not an error`() = runTest {
        // Arrange: two products found, but someone reserved one just before the update
        val scheduler = ExpiryScheduler(
            FakeReservationMaintenance(),
            FakeProductReader(expiredListings = listOf(FakeProductView(1), FakeProductView(2))),
            FakeProductStatusUpdater(expiredCount = 1),
            TestClock.fixed,
        )

        // Act
        val report = scheduler.runMaintenance()

        // Assert: the report shows what really happened
        assertEquals(1, report.expiredProducts)
    }

    @Test
    fun `a run with nothing to do reports zero`() = runTest {
        // Arrange
        val scheduler = ExpiryScheduler(
            FakeReservationMaintenance(),
            FakeProductReader(),
            FakeProductStatusUpdater(),
            TestClock.fixed,
        )

        // Act
        val report = scheduler.runMaintenance()

        // Assert
        assertEquals(MaintenanceReport(lapsedReservations = 0, expiredProducts = 0), report)
    }
}