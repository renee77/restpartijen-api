package com.restpartijen.api.seed

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.product.repository.ProductAllergensTable
import com.restpartijen.api.reservation.model.ReservationStatus
import com.restpartijen.api.reservation.repository.ReservationsTable
import com.restpartijen.api.security.service.PasswordHasher
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.Role
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/** One account to seed; every account gets the demo password. */
private data class SeedUser(
    val email: String,
    val displayName: String,
    val role: Role,
)

/**
 * One supplier to seed, linked to its account by [ownerEmail].
 * An email instead of a user id: the database chooses the id, so the data can only name the account.
 */
private data class SeedSupplier(
    val ownerEmail: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
)

/**
 * One product to seed. [key] is a name of our own, so a reservation can refer to the product before it has an id.
 * The times are offsets from the moment of seeding (§9.7): `6.hours` is six hours from now, `-2.hours` two hours ago.
 */
private data class SeedProduct(
    val key: String,
    val supplierEmail: String,
    val category: ProductCategory,
    val name: String,
    val priceCents: Long,
    val bestBefore: Duration,
    val pickupFrom: Duration,
    val pickupUntil: Duration,
    val status: ProductStatus = ProductStatus.LISTED,
    val quantity: Int = 1,
    val allergens: Set<Allergen> = emptySet(),
)

/**
 * One reservation to seed, linked to its product by [productKey] and to its collector by [collectorEmail].
 * [reservedAgo] is how long before the moment of seeding it was made; expiresAt is computed, as reserve() does.
 */
private data class SeedReservation(
    val productKey: String,
    val collectorEmail: String,
    val priceCents: Long,
    val reservedAgo: Duration,
    val status: ReservationStatus = ReservationStatus.ACTIVE,
)

object SeedData {
    private val users = listOf(
        SeedUser("admin@mail.com", "Pad Min", Role.ADMIN),
        SeedUser("bakker@mail.com", "Bakkerij De Korenschoof", Role.SUPPLIER),
        SeedUser("afhaler-a@mail.com", "Afhaler A", Role.COLLECTOR),
    )

    private val suppliers = listOf(
        SeedSupplier("bakker@mail.com", "Bakkerij De Korenschoof", "Ginnekenweg 12, Breda", 51.5795, 4.7820),
    )

    private val products = listOf(
        SeedProduct(
            key = "bakker-brood", supplierEmail = "bakker@mail.com", category = ProductCategory.FRESH,
            name = "Volkoren brood", priceCents = 349,
            bestBefore = 30.hours, pickupFrom = (-2).hours, pickupUntil = 6.hours,
            allergens = setOf(Allergen.CEREALS_CONTAINING_GLUTEN),
        ),
        SeedProduct(
            key = "bakker-croissants", supplierEmail = "bakker@mail.com", category = ProductCategory.FRESH,
            name = "Roomboter croissants", priceCents = 450,
            bestBefore = 20.hours, pickupFrom = (-2).hours, pickupUntil = 8.hours,
            status = ProductStatus.RESERVED,
            allergens = setOf(Allergen.CEREALS_CONTAINING_GLUTEN, Allergen.MILK),
        ),
    )

    private val reservations = listOf(
        SeedReservation("bakker-croissants", "afhaler-a@mail.com", priceCents = 270, reservedAgo = 1.hours),
    )

    // Demo only: public on purpose, because README.md and requests.http name it for every seed account.
    private const val DEMO_PASSWORD = "Demo-Restpartij-2026"

    fun seedIfEmpty(clock: Clock, hasher: PasswordHasher) {
        val isEmpty = transaction { UsersTable.selectAll().empty() }
        if (!isEmpty) return

        transaction {
            // Email → id, filled while inserting: the database chooses the ids, the data only knows the emails.
            val userIds = mutableMapOf<String, Long>()
            val supplierIds = mutableMapOf<String, Long>()

            for (user in users) {
                userIds[user.email] =
                    UsersTable.insert {
                        it[UsersTable.email] = user.email
                        it[UsersTable.displayName] = user.displayName
                        it[UsersTable.role] = user.role
                        it[UsersTable.passwordHash] = hasher.hashPassword(DEMO_PASSWORD)
                        it[UsersTable.createdAt] = clock.now()
                    }[UsersTable.id]
            }

            for (supplier in suppliers) {
                supplierIds[supplier.ownerEmail] =
                    SuppliersTable.insert {
                        // getValue fails loudly on a typo in ownerEmail, instead of storing a wrong link.
                        it[SuppliersTable.userId] = userIds.getValue(supplier.ownerEmail)
                        it[SuppliersTable.name] = supplier.name
                        it[SuppliersTable.address] = supplier.address
                        it[SuppliersTable.latitude] = supplier.latitude
                        it[SuppliersTable.longitude] = supplier.longitude
                    }[SuppliersTable.id]
            }

            // One moment for all products, so their offsets are relative to the same "now".
            val now = clock.now()
            val productIds = mutableMapOf<String, Long>()

            for (product in products) {
                productIds[product.key] =
                    ProductsTable.insert {
                        it[ProductsTable.supplierId] = supplierIds.getValue(product.supplierEmail)
                        it[ProductsTable.category] = product.category
                        it[ProductsTable.name] = product.name
                        it[ProductsTable.originalPriceCents] = product.priceCents
                        it[ProductsTable.bestBeforeAt] = now + product.bestBefore
                        it[ProductsTable.pickupFrom] = now + product.pickupFrom
                        it[ProductsTable.pickupUntil] = now + product.pickupUntil
                        it[ProductsTable.quantity] = product.quantity
                        it[ProductsTable.status] = product.status
                        it[ProductsTable.createdAt] = now
                    }[ProductsTable.id]

                for (allergen in product.allergens) {
                    ProductAllergensTable.insert {
                        it[ProductAllergensTable.productId] = productIds.getValue(product.key)
                        it[ProductAllergensTable.allergen] = allergen
                    }
                }
            }

            // The reservation needs the pickup window of its product, which only the data list knows.
            val productsByKey = products.associateBy { it.key }

            for (reservation in reservations) {
                val product = productsByKey.getValue(reservation.productKey)
                val reservedAt = now - reservation.reservedAgo

                ReservationsTable.insert {
                    it[ReservationsTable.productId] = productIds.getValue(reservation.productKey)
                    it[ReservationsTable.collectorId] = userIds.getValue(reservation.collectorEmail)
                    it[ReservationsTable.reservedPriceCents] = reservation.priceCents
                    it[ReservationsTable.reservedAt] = reservedAt
                    it[ReservationsTable.expiresAt] = minOf(reservedAt + 24.hours, now + product.pickupUntil)
                    it[ReservationsTable.status] = reservation.status
                }
            }
        }
    }
}