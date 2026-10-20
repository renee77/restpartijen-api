package com.restpartijen.api.reservation

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.persistence.dbQuery
import com.restpartijen.api.reservation.repository.ExposedReservationRepository
import com.restpartijen.api.reservation.repository.ReservationsTable
import com.restpartijen.api.reservation.service.ReservationService
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.ProductStatusUpdater
import com.restpartijen.api.testsupport.FixedClock
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import com.restpartijen.api.testsupport.insertProduct
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
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

/**
 * Changes the status directly in ProductsTable, the same way F1's updater does.
 * ProductsTable lives in persistence, so F2 may use it (issue #36); F1's own class may not be imported (GI-5).
 */
private class TableStatusUpdater : ProductStatusUpdater {
    override suspend fun markReserved(productId: Long): Boolean = dbQuery {
        ProductsTable.update({ (ProductsTable.id eq productId) and (ProductsTable.status eq ProductStatus.LISTED) }) {
            it[ProductsTable.status] = ProductStatus.RESERVED
        } > 0
    }

    override suspend fun markListed(productId: Long): Boolean = TODO("not needed here")
    override suspend fun markCollected(productId: Long): Boolean = TODO("not needed here")
    override suspend fun markExpired(productIds: List<Long>): Int = TODO("not needed here")
    override suspend fun markRemoved(productId: Long): Boolean = TODO("not needed here")
}