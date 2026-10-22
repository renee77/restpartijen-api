package com.restpartijen.api.pricing

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.pricing.model.MaintenanceReport
import com.restpartijen.api.pricing.service.ExpiryScheduler
import com.restpartijen.api.product.repository.ExposedProductReader
import com.restpartijen.api.product.repository.ExposedProductRepository
import com.restpartijen.api.product.repository.ExposedProductStatusUpdater
import com.restpartijen.api.product.repository.ProductAllergensTable
import com.restpartijen.api.reservation.model.ReservationStatus
import com.restpartijen.api.reservation.repository.ExposedReservationRepository
import com.restpartijen.api.reservation.repository.ReservationsTable
import com.restpartijen.api.reservation.service.ReservationMaintenanceService
import com.restpartijen.api.seed.SeedData
import com.restpartijen.api.security.service.PasswordHasher
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.testsupport.FixedClock
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

/**
 * The status monitoring on a real H2 database with the seed data and the real implementations
 * of F1 and F2 behind the contracts (US-08, TC-08). Only the clock is fixed.
 */
class ExpirySchedulerIntegrationTest {

    // Whole hours: the database may store fewer digits than Instant has.
    private val now = Instant.parse("2026-10-20T12:00:00Z")
    private val clock = FixedClock(now)

    private lateinit var scheduler: ExpiryScheduler

    @BeforeTest
    fun setUp() {
        // Every test gets its own empty database, filled with the seed data for this one moment
        createEmptyTestDatabase(UsersTable, SuppliersTable, ProductsTable, ProductAllergensTable, ReservationsTable)
        SeedData.seedIfEmpty(clock, PasswordHasher())

        // The real parts behind the contracts, wired like configureDependencies() does, but with the fixed clock
        val productReader = ExposedProductReader(ExposedProductRepository(clock))
        val statusUpdater = ExposedProductStatusUpdater()
        val reservationMaintenance = ReservationMaintenanceService(
            productReader, statusUpdater, ExposedReservationRepository()
        )
        scheduler = ExpiryScheduler(reservationMaintenance, productReader, statusUpdater, clock)
    }

    /** The status of every product in the database, by product name. */
    private fun productStatuses(): Map<String, ProductStatus> = transaction {
        ProductsTable.selectAll().associate { it[ProductsTable.name] to it[ProductsTable.status] }
    }

    /** The status of the reservation of every product, by product name. */
    private fun reservationStatuses(): Map<String, ReservationStatus> = transaction {
        val names = ProductsTable.selectAll().associate { it[ProductsTable.id] to it[ProductsTable.name] }
        ReservationsTable.selectAll().associate {
            names.getValue(it[ReservationsTable.productId]) to it[ReservationsTable.status]
        }
    }

    @Test
    fun `one run lapses the overdue reservations and expires the products past their date`() = runTest {
        // Act
        val report = scheduler.runMaintenance()

        // Assert: three reservations lapsed (wraps, 25 hours old, casinobrood) and two products expired
        assertEquals(MaintenanceReport(lapsedReservations = 3, expiredProducts = 2), report)

        val products = productStatuses()
        // LISTED and past its date
        assertEquals(ProductStatus.EXPIRED, products["Griekse yoghurt"])
        // RESERVED and past its date: it lapses first and then expires. LISTED here would mean the steps ran in the wrong order.
        assertEquals(ProductStatus.EXPIRED, products["Wit casinobrood"])
        // Lapsed reservations put their products back on the list
        assertEquals(ProductStatus.LISTED, products["Wraps kip-kerrie"])
        assertEquals(ProductStatus.LISTED, products["Volle yoghurt 5 liter"])
        // Reservations that are still valid stay as they are
        assertEquals(ProductStatus.RESERVED, products["Roomboter croissants"])
        assertEquals(ProductStatus.RESERVED, products["Kipfilet 500 g"])
        assertEquals(ProductStatus.RESERVED, products["Verse tomatensoep 1 liter"])
        // The end states do not change
        assertEquals(ProductStatus.COLLECTED, products["Appeltaart"])
        assertEquals(ProductStatus.EXPIRED, products["Karnemelk 1 liter"])
        assertEquals(ProductStatus.REMOVED, products["Appelsap 1,5 liter"])

        val reservations = reservationStatuses()
        assertEquals(ReservationStatus.LAPSED, reservations["Wraps kip-kerrie"])
        assertEquals(ReservationStatus.LAPSED, reservations["Volle yoghurt 5 liter"])
        assertEquals(ReservationStatus.LAPSED, reservations["Wit casinobrood"])
        assertEquals(ReservationStatus.ACTIVE, reservations["Roomboter croissants"])
        assertEquals(ReservationStatus.ACTIVE, reservations["Kipfilet 500 g"])
        assertEquals(ReservationStatus.ACTIVE, reservations["Verse tomatensoep 1 liter"])
    }

    @Test
    fun `a second run right after the first changes nothing`() = runTest {
        // Arrange: the first run does the work
        scheduler.runMaintenance()
        val productsAfterFirstRun = productStatuses()
        val reservationsAfterFirstRun = reservationStatuses()

        // Act
        val report = scheduler.runMaintenance()

        // Assert: nothing is counted and nothing has changed (idempotent, §5.8)
        assertEquals(MaintenanceReport(lapsedReservations = 0, expiredProducts = 0), report)
        assertEquals(productsAfterFirstRun, productStatuses())
        assertEquals(reservationsAfterFirstRun, reservationStatuses())
    }

    @Test
    fun `a reservation older than 24 hours lapses even when the pickup window is still open`() = runTest {
        // Arrange: the premise, read from the database so the test says why this product lapses
        val (reservedAt, pickupUntil) = transaction {
            val productId = ProductsTable.selectAll()
                .single { it[ProductsTable.name] == "Volle yoghurt 5 liter" }[ProductsTable.id]
            val reservedAt = ReservationsTable.selectAll()
                .single { it[ReservationsTable.productId] == productId }[ReservationsTable.reservedAt]
            val pickupUntil = ProductsTable.selectAll()
                .single { it[ProductsTable.id] == productId }[ProductsTable.pickupUntil]
            reservedAt to pickupUntil
        }
        assertTrue(reservedAt <= now - 24.hours, "the reservation should be older than 24 hours")
        assertTrue(pickupUntil > now, "the pickup window should still be open")
        assertEquals(ProductStatus.RESERVED, productStatuses()["Volle yoghurt 5 liter"])

        // Act
        scheduler.runMaintenance()

        // Assert: the 24-hour limit lapsed it, and the product is back on the list
        assertEquals(ReservationStatus.LAPSED, reservationStatuses()["Volle yoghurt 5 liter"])
        assertEquals(ProductStatus.LISTED, productStatuses()["Volle yoghurt 5 liter"])
    }
}