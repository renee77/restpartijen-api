package com.restpartijen.api.pricing

import com.restpartijen.api.pricing.service.AdminProductService
import com.restpartijen.api.shared.IllegalStateTransitionException
import com.restpartijen.api.shared.NotFoundException
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** AdminProductService with fakes behind the contracts (US-09): the order of the calls and the refusals. */
class AdminProductServiceTest {

    @Test
    fun `deleting a reserved product lapses the reservation before removing it`() = runTest {
        // Arrange: one shared call list, so the order across both fakes is visible
        createEmptyTestDatabase()
        val calls = mutableListOf<String>()
        val service = AdminProductService(
            productReader = FakeProductReader(products = listOf(FakeProductView(1, ProductStatus.RESERVED))),
            productStatusUpdater = FakeProductStatusUpdater(calls = calls),
            reservationMaintenance = FakeReservationMaintenance(calls = calls),
        )

        // Act
        service.delete(1)

        // Assert
        assertEquals(listOf("lapseActiveFor(1)", "markRemoved(1)"), calls)
    }

    @Test
    fun `trying to delete an unknown id will give an exception`() = runTest {
        // Arrange
        createEmptyTestDatabase()
        val calls = mutableListOf<String>()
        val service = AdminProductService(
            productReader = FakeProductReader(products = listOf()),
            productStatusUpdater = FakeProductStatusUpdater(calls = calls),
            reservationMaintenance = FakeReservationMaintenance(calls = calls),
        )

        // Act and Assert
        assertFailsWith<NotFoundException> { service.delete(1) }
    }

    @Test
    fun `trying to delete a listed product will call markRemoved and not lapseActivefor`() = runTest {
        // Arrange
        createEmptyTestDatabase()
        val calls = mutableListOf<String>()
        val service = AdminProductService(
            productReader = FakeProductReader(products = listOf(FakeProductView(1, ProductStatus.LISTED))),
            productStatusUpdater = FakeProductStatusUpdater(calls = calls),
            reservationMaintenance = FakeReservationMaintenance(calls = calls),
        )

        // Act
        service.delete(1)

        // Assert
        assertEquals(listOf("markRemoved(1)"), calls)
    }

    @Test
    fun `delete of a product with status collected or expired will give an exception`() = runTest {
        // Arrange
        createEmptyTestDatabase()
        val calls = mutableListOf<String>()
        val service = AdminProductService(
            productReader = FakeProductReader(
                products = listOf(
                    FakeProductView(1, ProductStatus.EXPIRED),
                    FakeProductView(2, ProductStatus.COLLECTED),
                ),
            ),
            productStatusUpdater = FakeProductStatusUpdater(calls = calls),
            reservationMaintenance = FakeReservationMaintenance(calls = calls),
        )

        // Act and Assert
        assertFailsWith<IllegalStateTransitionException> { service.delete(1) }
        assertFailsWith<IllegalStateTransitionException> { service.delete(2) }
    }

    @Test
    fun `deleting fails when markRemoved returns false`() = runTest {
        // Arrange: a LISTED product, but the updater reports that removing did not work
        createEmptyTestDatabase()
        val service = AdminProductService(
            productReader = FakeProductReader(products = listOf(FakeProductView(1, ProductStatus.LISTED))),
            productStatusUpdater = FakeProductStatusUpdater(removeSucceeds = false),
            reservationMaintenance = FakeReservationMaintenance(),
        )

        // Act and assert
        assertFailsWith<IllegalStateTransitionException> { service.delete(1) }
    }

    @Test
    fun `overview without a filter asks for every status except REMOVED`() = runTest {
        // Arrange: keep the reader in a val, so the test can read what it was asked
        val reader = FakeProductReader()
        val service = AdminProductService(reader, FakeProductStatusUpdater(), FakeReservationMaintenance())

        // Act
        service.overview(emptySet())

        // Assert
        assertEquals(ProductStatus.entries.toSet() - ProductStatus.REMOVED, reader.requestedStatuses)
    }

    @Test
    fun `overview with a filter passes exactly those statuses on`() = runTest {
        // Arrange
        val reader = FakeProductReader()
        val service = AdminProductService(reader, FakeProductStatusUpdater(), FakeReservationMaintenance())
        val requested = setOf(ProductStatus.LISTED, ProductStatus.RESERVED)

        // Act
        service.overview(requested)

        // Assert
        assertEquals(requested, reader.requestedStatuses)
    }
}
