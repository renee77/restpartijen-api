package com.restpartijen.api.reservation

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.reservation.repository.ExposedReservationRepository
import com.restpartijen.api.reservation.repository.ReservationsTable
import com.restpartijen.api.reservation.service.ReservationService
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.testsupport.FixedClock
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import com.restpartijen.api.testsupport.insertProduct
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * Proves §9.6: reserving changes two tables in one transaction, so a failure undoes both.
 * Uses the real database; only reading the product and the price are fakes.
 */
class ReservationTransactionTest {

    private val now = Instant.parse("2026-10-20T12:00:00Z")

    @Test
    fun `when storing the reservation fails, the product goes back to LISTED`() = runTest {
        // Arrange: a real product row, and a collector id that does not exist
        createEmptyTestDatabase(UsersTable, SuppliersTable, ProductsTable, ReservationsTable)
        val productId = insertProduct()
        val unknownCollectorId = 999_999L
        val service = ReservationService(
            FakeProductReader(listOf(FakeProduct(id = productId, pickupWindow = PickupWindow(now - 1.hours, now + 1.days)))),
            TableStatusUpdater(),
            FakePriceProvider(Money.fromCents(199)),
            ExposedReservationRepository(),
            FixedClock(now),
        )

        // Act: markReserved succeeds, then the foreign key on collector_id refuses the reservation
        assertFails { service.reserve(productId, unknownCollectorId) }

        // Assert: the transaction rolled back, so the product is LISTED again
        assertEquals(ProductStatus.LISTED, statusOf(productId))
    }

    private fun statusOf(productId: Long): ProductStatus = transaction {
        ProductsTable.selectAll().where { ProductsTable.id eq productId }.single()[ProductsTable.status]
    }
}