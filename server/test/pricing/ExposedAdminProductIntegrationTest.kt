package com.restpartijen.api.pricing

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.pricing.service.AdminProductService
import com.restpartijen.api.product.repository.ExposedProductReader
import com.restpartijen.api.product.repository.ExposedProductRepository
import com.restpartijen.api.product.repository.ExposedProductStatusUpdater
import com.restpartijen.api.product.repository.ProductAllergensTable
import com.restpartijen.api.product.repository.SupplierOpeningHoursTable
import com.restpartijen.api.reservation.model.ReservationStatus
import com.restpartijen.api.reservation.repository.ExposedReservationRepository
import com.restpartijen.api.reservation.repository.ReservationsTable
import com.restpartijen.api.reservation.service.ReservationMaintenanceService
import com.restpartijen.api.seed.SeedData
import com.restpartijen.api.security.service.PasswordHasher
import com.restpartijen.api.shared.IllegalStateTransitionException
import com.restpartijen.api.shared.NotFoundException
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.ProductStatusUpdater
import com.restpartijen.api.testsupport.FixedClock
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * US-09 on a real H2 database with the seed data and the real F1 and F2 parts behind the contracts.
 * It proves what the fakes cannot: that removing and lapsing share one transaction.
 */
class ExposedAdminProductIntegrationTest {

    private val clock = FixedClock(Instant.parse("2026-10-20T12:00:00Z"))

    private lateinit var productReader: ExposedProductReader
    private lateinit var statusUpdater: ExposedProductStatusUpdater
    private lateinit var reservationMaintenance: ReservationMaintenanceService
    private lateinit var service: AdminProductService

    // Wraps the real updater, but a removal never works. This lets the test fail the second step.
    private class FailingRemoveUpdater(real: ProductStatusUpdater) : ProductStatusUpdater by real {
        override suspend fun markRemoved(productId: Long): Boolean = false
    }

    @BeforeTest
    fun setUp() {
        // SupplierOpeningHoursTable too: the seed writes the opening hours of every supplier (US-10).
        createEmptyTestDatabase(
            UsersTable, SuppliersTable, ProductsTable, ProductAllergensTable, ReservationsTable,
            SupplierOpeningHoursTable,
        )
        SeedData.seedIfEmpty(clock, PasswordHasher())

        productReader = ExposedProductReader(ExposedProductRepository(clock))
        statusUpdater = ExposedProductStatusUpdater()
        reservationMaintenance = ReservationMaintenanceService(
            productReader, statusUpdater, ExposedReservationRepository()
        )
        service = AdminProductService(productReader, statusUpdater, reservationMaintenance)
    }

    // ---- Helpers that read straight from the database ----

    private fun idOf(name: String): Long = transaction {
        ProductsTable.selectAll().single { it[ProductsTable.name] == name }[ProductsTable.id]
    }

    private fun idWithStatus(status: ProductStatus): Long = transaction {
        ProductsTable.selectAll().first { it[ProductsTable.status] == status }[ProductsTable.id]
    }

    private fun statusOf(productId: Long): ProductStatus = transaction {
        ProductsTable.selectAll().single { it[ProductsTable.id] == productId }[ProductsTable.status]
    }

    private fun reservationStatusOf(productId: Long): ReservationStatus = transaction {
        ReservationsTable.selectAll().single { it[ReservationsTable.productId] == productId }[ReservationsTable.status]
    }

    // ---- delete ----

    @Test
    fun `deleting a reserved product lapses its reservation and removes it`() = runTest {
        // Arrange: a product with a valid, active reservation
        val id = idOf("Roomboter croissants")
        assertEquals(ProductStatus.RESERVED, statusOf(id))
        assertEquals(ReservationStatus.ACTIVE, reservationStatusOf(id))

        // Act
        service.delete(id)

        // Assert
        assertEquals(ProductStatus.REMOVED, statusOf(id))
        assertEquals(ReservationStatus.LAPSED, reservationStatusOf(id))
    }

    @Test
    fun `a failing removal rolls the lapse back`() = runTest {
        // Arrange: the lapse works, but the removal after it fails
        val failingService = AdminProductService(
            productReader, FailingRemoveUpdater(statusUpdater), reservationMaintenance
        )
        val id = idOf("Roomboter croissants")

        // Act
        assertFailsWith<IllegalStateTransitionException> { failingService.delete(id) }

        // Assert: nothing has changed, so the lapse was rolled back with the failed removal
        assertEquals(ProductStatus.RESERVED, statusOf(id))
        assertEquals(ReservationStatus.ACTIVE, reservationStatusOf(id))
    }

    @Test
    fun `deleting a listed product removes it`() = runTest {
        val id = idWithStatus(ProductStatus.LISTED)

        service.delete(id)

        assertEquals(ProductStatus.REMOVED, statusOf(id))
    }

    @Test
    fun `a collected or expired product cannot be removed`() = runTest {
        for (status in listOf(ProductStatus.COLLECTED, ProductStatus.EXPIRED)) {
            val id = idWithStatus(status)

            assertFailsWith<IllegalStateTransitionException> { service.delete(id) }

            assertEquals(status, statusOf(id), "a $status product must stay as it is")
        }
    }

    @Test
    fun `a removed product counts as not found`() = runTest {
        val id = idWithStatus(ProductStatus.REMOVED)

        assertFailsWith<NotFoundException> { service.delete(id) }
    }

    // ---- overview ----

    @Test
    fun `the overview without a filter hides removed products`() = runTest {
        val statuses = service.overview(emptySet()).map { it.status }.toSet()

        assertEquals(ProductStatus.entries.toSet() - ProductStatus.REMOVED, statuses)
    }

    @Test
    fun `the overview with REMOVED shows only removed products`() = runTest {
        val products = service.overview(setOf(ProductStatus.REMOVED))

        assertTrue(products.isNotEmpty())
        assertTrue(products.all { it.status == ProductStatus.REMOVED })
        assertTrue(products.any { it.name == "Appelsap 1,5 liter" })
    }

    @Test
    fun `the overview with one status shows only that status`() = runTest {
        val products = service.overview(setOf(ProductStatus.LISTED))

        assertTrue(products.isNotEmpty())
        assertTrue(products.all { it.status == ProductStatus.LISTED })
    }

    @Test
    fun `the overview with two statuses shows exactly those`() = runTest {
        val requested = setOf(ProductStatus.RESERVED, ProductStatus.COLLECTED)

        val statuses = service.overview(requested).map { it.status }.toSet()

        assertEquals(requested, statuses)
    }
}