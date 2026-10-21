package com.restpartijen.api.reservation

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.persistence.dbQuery
import com.restpartijen.api.reservation.model.Reservation
import com.restpartijen.api.reservation.repository.ReservationRepository
import com.restpartijen.api.reservation.repository.ReservationsTable
import com.restpartijen.api.reservation.service.ReservationMaintenanceService
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import com.restpartijen.api.testsupport.insertProduct
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * K-5 / issue #48: lapsing returns false and never throws, and on false nothing of the lapse stays behind.
 * Real database for the product status; storing the reservation is made to fail on purpose.
 */
class ReservationLapseRollbackTest {

    private val now = Instant.parse("2026-10-20T12:00:00Z")

    @BeforeTest
    fun setup() = createEmptyTestDatabase(UsersTable, SuppliersTable, ProductsTable, ReservationsTable)

    @Test
    fun `when storing the lapse fails, lapseActiveFor returns false and the product stays RESERVED`() = runTest {
        // Arrange
        val productId = reservedProductRow()
        val service = serviceWith(listOf(productId), failingReservationIds = setOf(1L))

        // Act
        val lapsed = service.lapseActiveFor(productId)

        // Assert: false instead of an exception, and markListed was undone
        assertFalse(lapsed)
        assertEquals(ProductStatus.RESERVED, statusOf(productId))
    }

    @Test
    fun `inside the caller's transaction a failed lapse is undone and the transaction stays usable`() = runTest {
        // Arrange: like AdminProductService.delete, which calls lapseActiveFor inside its own dbQuery
        val productId = reservedProductRow()
        val service = serviceWith(listOf(productId), failingReservationIds = setOf(1L))

        // Act
        val (lapsed, statusInside) = dbQuery {
            val result = service.lapseActiveFor(productId)
            result to ProductsTable.selectAll().where { ProductsTable.id eq productId }.single()[ProductsTable.status]
        }

        // Assert: the caller sees RESERVED within its transaction, and after the commit
        assertFalse(lapsed)
        assertEquals(ProductStatus.RESERVED, statusInside)
        assertEquals(ProductStatus.RESERVED, statusOf(productId))
    }

    @Test
    fun `lapseOverdue keeps going when one reservation fails`() = runTest {
        // Arrange: two overdue reservations; storing the first one fails
        val failing = reservedProductRow()
        val working = reservedProductRow()
        val service = serviceWith(listOf(failing, working), failingReservationIds = setOf(1L))

        // Act
        val count = service.lapseOverdue(now)

        // Assert: only the second lapsed; the first is untouched
        assertEquals(1, count)
        assertEquals(ProductStatus.RESERVED, statusOf(failing))
        assertEquals(ProductStatus.LISTED, statusOf(working))
    }

    /** One overdue ACTIVE reservation per product (ids 1, 2, ...); update fails for [failingReservationIds]. */
    private fun serviceWith(productIds: List<Long>, failingReservationIds: Set<Long>): ReservationMaintenanceService {
        val stored = productIds.mapIndexed { index, productId ->
            Reservation(
                index + 1L, productId, collectorId = 7, reservedPrice = Money.fromCents(199),
                reservedAt = now - 25.hours, expiresAt = now - 1.hours,
            )
        }
        val fake = FakeReservationRepository(stored)
        val reservations = object : ReservationRepository by fake {
            override suspend fun update(item: Reservation): Boolean =
                if (item.id in failingReservationIds) false else fake.update(item)
        }
        val reader = FakeProductReader(productIds.map {
            FakeProduct(id = it, status = ProductStatus.RESERVED, pickupWindow = PickupWindow(now - 2.days, now + 2.days))
        })
        return ReservationMaintenanceService(reader, TableStatusUpdater(), reservations)
    }

    private fun reservedProductRow(): Long {
        val productId = insertProduct()
        transaction {
            ProductsTable.update({ ProductsTable.id eq productId }) { it[status] = ProductStatus.RESERVED }
        }
        return productId
    }

    private fun statusOf(productId: Long): ProductStatus = transaction {
        ProductsTable.selectAll().where { ProductsTable.id eq productId }.single()[ProductsTable.status]
    }
}
