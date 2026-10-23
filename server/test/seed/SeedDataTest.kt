package com.restpartijen.api.seed

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.pricing.model.DiscountPolicyResolver
import com.restpartijen.api.product.repository.ProductAllergensTable
import com.restpartijen.api.product.repository.SupplierOpeningHoursTable
import com.restpartijen.api.reservation.model.ReservationStatus
import com.restpartijen.api.reservation.repository.ReservationsTable
import com.restpartijen.api.security.service.PasswordHasher
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.FixedClock
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * The seed data: what FTD §9.7 and the requests of Lonneke (F3) and Stefan (F2) need for tests and the demo.
 * Each test seeds its own empty database with a fixed clock, so every date is predictable.
 */
class SeedDataTest {

    // Whole hours: the database may store fewer digits than Instant has.
    private val now = Instant.parse("2026-10-20T12:00:00Z")

    // The one demo password of all six accounts; README.md and requests.http name it too.
    private val demoPassword = "Demo-Restpartij-2026"

    private val hasher = PasswordHasher()

    // The rows as plain values, so the tests can filter and count them without Exposed.
    private data class UserRow(val id: Long, val email: String, val role: Role, val passwordHash: String)
    private data class SupplierRow(val id: Long, val userId: Long)
    private data class ProductRow(
        val id: Long, val supplierId: Long, val category: ProductCategory, val priceCents: Long,
        val bestBeforeAt: Instant, val status: ProductStatus, val pickupFrom: Instant, val pickupUntil: Instant,
    )
    private data class ReservationRow(
        val productId: Long, val collectorId: Long, val priceCents: Long,
        val reservedAt: Instant, val expiresAt: Instant, val status: ReservationStatus,
    )

    private fun users() = transaction {
        UsersTable.selectAll().map {
            UserRow(it[UsersTable.id], it[UsersTable.email], it[UsersTable.role], it[UsersTable.passwordHash])
        }
    }

    private fun suppliers() = transaction {
        SuppliersTable.selectAll().map { SupplierRow(it[SuppliersTable.id], it[SuppliersTable.userId]) }
    }

    private fun products() = transaction {
        ProductsTable.selectAll().map {
            ProductRow(
                it[ProductsTable.id], it[ProductsTable.supplierId], it[ProductsTable.category],
                it[ProductsTable.originalPriceCents], it[ProductsTable.bestBeforeAt], it[ProductsTable.status],
                it[ProductsTable.pickupFrom], it[ProductsTable.pickupUntil],
            )
        }
    }

    private fun reservations() = transaction {
        ReservationsTable.selectAll().map {
            ReservationRow(
                it[ReservationsTable.productId], it[ReservationsTable.collectorId], it[ReservationsTable.reservedPriceCents],
                it[ReservationsTable.reservedAt], it[ReservationsTable.expiresAt], it[ReservationsTable.status],
            )
        }
    }

    private fun userId(email: String) = users().single { it.email == email }.id

    private fun supplierIdOf(email: String) = suppliers().single { it.userId == userId(email) }.id

    private fun ProductRow.discountPercentage() =
        DiscountPolicyResolver.resolve(category).discountPercentage(bestBeforeAt - now)

    // Integer division is precise enough: the tests only compare this price with a limit.
    private fun ProductRow.currentPriceCents() = priceCents * (100 - discountPercentage()) / 100

    private fun ProductRow.windowIsOpen() = pickupFrom <= now && now < pickupUntil

    private fun ProductRow.isFresh() = bestBeforeAt > now

    @BeforeTest
    fun seedAnEmptyDatabase() {
        createEmptyTestDatabase(
            UsersTable, SuppliersTable, ProductsTable, ProductAllergensTable, ReservationsTable,
            SupplierOpeningHoursTable,
        )
        SeedData.seedIfEmpty(FixedClock(now), hasher)
    }

    // ---- Accounts (§9.7, Lonneke) ----

    @Test
    fun `seeds the six demo accounts with their roles`() {
        val roleByEmail = users().associate { it.email to it.role }

        assertEquals(
            mapOf(
                "admin@mail.com" to Role.ADMIN,
                "supermarkt@mail.com" to Role.SUPPLIER,
                "bakker@mail.com" to Role.SUPPLIER,
                "groothandel@mail.com" to Role.SUPPLIER,
                "afhaler-a@mail.com" to Role.COLLECTOR,
                "afhaler-b@mail.com" to Role.COLLECTOR,
            ),
            roleByEmail,
        )
    }

    @Test
    fun `every account logs in with the demo password`() {
        // A hash, not the password itself: /auth/login only works when the stored value is a real hash.
        users().forEach {
            assertTrue(hasher.verifyPassword(demoPassword, it.passwordHash), "Demo password fails for ${it.email}")
        }
    }

    @Test
    fun `every supplier account has its own supplier row`() {
        val supplierUserIds = users().filter { it.role == Role.SUPPLIER }.map { it.id }.toSet()

        assertEquals(supplierUserIds, suppliers().map { it.userId }.toSet())
    }

    @Test
    fun `every supplier has opening hours`() {
        // A supplier always has opening hours (US-10, decision 23 Oct 2026); without them no product gets a window.
        val suppliersWithHours = transaction {
            SupplierOpeningHoursTable.selectAll().map { it[SupplierOpeningHoursTable.supplierId] }.toSet()
        }

        assertEquals(suppliers().map { it.id }.toSet(), suppliersWithHours)
    }

    // ---- Products (§9.7) ----

    @Test
    fun `every supplier has at least five products and the bakery sells no frozen`() {
        val byCategory = products().groupBy({ it.supplierId }, { it.category })
        val allCategories = ProductCategory.entries.toSet()

        listOf("supermarkt@mail.com", "bakker@mail.com", "groothandel@mail.com").forEach { email ->
            assertTrue(byCategory.getValue(supplierIdOf(email)).size >= 5, "Fewer than five products for $email")
        }
        assertEquals(allCategories, byCategory.getValue(supplierIdOf("supermarkt@mail.com")).toSet())
        assertEquals(allCategories, byCategory.getValue(supplierIdOf("groothandel@mail.com")).toSet())
        assertEquals(
            setOf(ProductCategory.FRESH, ProductCategory.AMBIENT),
            byCategory.getValue(supplierIdOf("bakker@mail.com")).toSet(),
        )
    }

    @Test
    fun `there is at least one product in every status`() {
        assertEquals(ProductStatus.entries.toSet(), products().map { it.status }.toSet())
    }

    @Test
    fun `listed products cover all ten discount tiers`() {
        // FRESH 0/10/40/60, FROZEN 0/15/40, AMBIENT 0/15/40: ten pairs of category and percentage (§9.4).
        val tiers = products()
            .filter { it.status == ProductStatus.LISTED && it.isFresh() }
            .map { it.category to it.discountPercentage() }
            .toSet()

        assertEquals(10, tiers.size, "Tiers found: $tiers")
    }

    @Test
    fun `seeding a second time adds nothing`() {
        val before = listOf(users().size, products().size, reservations().size)

        SeedData.seedIfEmpty(FixedClock(now), hasher)

        assertEquals(before, listOf(users().size, products().size, reservations().size))
    }

    // ---- Consistency between products and reservations (§9.5, Stefan) ----

    @Test
    fun `reservations match the status of their product`() {
        val reservationsByProduct = reservations().groupBy { it.productId }

        products().forEach { product ->
            val statuses = reservationsByProduct[product.id].orEmpty().map { it.status }
            when (product.status) {
                ProductStatus.RESERVED -> assertEquals(1, statuses.count { it == ReservationStatus.ACTIVE }, "Product ${product.id}")
                ProductStatus.COLLECTED -> assertEquals(1, statuses.count { it == ReservationStatus.COLLECTED }, "Product ${product.id}")
                else -> assertTrue(ReservationStatus.ACTIVE !in statuses, "Product ${product.id} has an ACTIVE reservation")
            }
        }
    }

    @Test
    fun `every reservation expires as reserve() computes it and costs at most the original price`() {
        val productById = products().associateBy { it.id }

        reservations().forEach {
            val product = productById.getValue(it.productId)
            assertEquals(minOf(it.reservedAt + 24.hours, product.pickupUntil), it.expiresAt, "Product ${product.id}")
            assertTrue(it.priceCents <= product.priceCents, "Product ${product.id}")
        }
    }

    // ---- Demo scenarios of Stefan (F2) ----

    @Test
    fun `there are products to reserve live and one whose window has passed`() {
        val listedAndFresh = products().filter { it.status == ProductStatus.LISTED && it.isFresh() }

        assertTrue(listedAndFresh.count { it.windowIsOpen() } >= 2, "Fewer than two products to reserve live")
        assertTrue(listedAndFresh.any { now >= it.pickupUntil }, "No product whose pickup window has passed")
    }

    @Test
    fun `a product above three euros costs less than three after discount`() {
        // Shows that the maxPrice filter uses the discounted price.
        assertTrue(
            products().any {
                it.status == ProductStatus.LISTED && it.isFresh() && it.priceCents > 300 && it.currentPriceCents() < 300
            }
        )
    }

    @Test
    fun `collector A has every kind of reservation for the demo`() {
        val productById = products().associateBy { it.id }
        val ofA = reservations().filter { it.collectorId == userId("afhaler-a@mail.com") }

        // Two to cancel and collect live; they must outlast the first maintenance runs.
        val live = ofA.filter {
            it.status == ReservationStatus.ACTIVE && it.expiresAt >= now + 3.hours &&
                productById.getValue(it.productId).windowIsOpen()
        }
        assertTrue(live.size >= 2, "Fewer than two live ACTIVE reservations")
        // History for GET /reservations, and 409 on confirming.
        assertTrue(ofA.any { it.status == ReservationStatus.COLLECTED })
        assertTrue(ofA.any { it.status == ReservationStatus.CANCELLED })
        assertTrue(ofA.any { it.status == ReservationStatus.LAPSED })
    }

    @Test
    fun `collector B has a reservation of their own`() {
        assertTrue(reservations().any { it.collectorId == userId("afhaler-b@mail.com") })
    }

    @Test
    fun `a twenty-hour-old reservation keeps a price above the current one`() {
        // The product has moved to a higher tier since; the reserved price must not follow (§5.5).
        val productById = products().associateBy { it.id }

        assertTrue(
            reservations().any {
                it.status == ReservationStatus.ACTIVE && it.reservedAt == now - 20.hours &&
                    it.priceCents > productById.getValue(it.productId).currentPriceCents()
            }
        )
    }

    // ---- Status monitoring of Lonneke (US-08) ----

    @Test
    fun `there is work for the first maintenance run`() {
        val products = products()
        val reservations = reservations()

        assertTrue(products.any { it.status == ProductStatus.LISTED && !it.isFresh() }, "No listed product past its date")
        assertTrue(products.any { it.status == ProductStatus.RESERVED && !it.isFresh() }, "No reserved product past its date")
        // lapseOverdue: one past its pickup window, one older than 24 hours.
        val overdue = reservations.filter { it.status == ReservationStatus.ACTIVE && it.expiresAt <= now }
        assertTrue(overdue.any { it.reservedAt > now - 24.hours }, "No reservation past its pickup window")
        assertTrue(overdue.any { it.reservedAt <= now - 24.hours }, "No reservation older than 24 hours")
    }
}
