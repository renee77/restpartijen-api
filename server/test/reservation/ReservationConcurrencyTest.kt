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
import com.restpartijen.api.testsupport.insertUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * W-24 / §5.5: when two collectors reserve the same product at the same moment, exactly one succeeds.
 * Real database: the conditional UPDATE in markReserved decides, not the Kotlin code.
 */
class ReservationConcurrencyTest {

    private val now = Instant.parse("2026-10-20T12:00:00Z")

    @Test
    fun `of two simultaneous reservations exactly one succeeds`() = runTest {
        // Arrange: one real product, two real collectors
        createEmptyTestDatabase(UsersTable, SuppliersTable, ProductsTable, ReservationsTable)
        val productId = insertProduct()
        val collectors = listOf(insertUser(), insertUser())
        // The fake reader always says LISTED, so both pass the state machine: only the database can stop one
        val service = ReservationService(
            FakeProductReader(
                listOf(FakeProduct(id = productId, pickupWindow = PickupWindow(now - 1.hours, now + 1.days))),
            ),
            TableStatusUpdater(),
            FakePriceProvider(Money.fromCents(199)),
            ExposedReservationRepository(),
            FixedClock(now),
        )

        // Act: both on real threads at the same time
        val results = coroutineScope {
            collectors.map { collectorId ->
                async(Dispatchers.IO) { runCatching { service.reserve(productId, collectorId) } }
            }.awaitAll()
        }

        // Assert: one winner, one stored reservation, product RESERVED
        assertEquals(1, results.count { it.isSuccess }, "results: $results")
        assertEquals(1L, transaction { ReservationsTable.selectAll().count() })
        assertEquals(
            ProductStatus.RESERVED,
            transaction {
                ProductsTable.selectAll().where { ProductsTable.id eq productId }.single()[ProductsTable.status]
            },
        )
    }
}
