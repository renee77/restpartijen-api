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
import kotlin.time.Duration.Companion.days
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

/**
 * The start state of the database for the demo and manual tests (FTD §9.7).
 *
 * Lives in its own package next to Application.kt, because it writes into the tables of every feature:
 * in persistence or shared it would make a lower layer import a feature (open point 20).
 * It writes into the tables directly instead of through the contracts; §12.1 allows that for the seed only.
 */
object SeedData {
    // Six accounts (§9.7). The emails are lower case, because login normalises them.
    private val users = listOf(
        SeedUser("admin@mail.com", "Pad Min", Role.ADMIN),
        SeedUser("supermarkt@mail.com", "Supermarkt Breda Centrum", Role.SUPPLIER),
        SeedUser("bakker@mail.com", "Bakkerij De Korenschoof", Role.SUPPLIER),
        SeedUser("groothandel@mail.com", "Groothandel Vers & Vries", Role.SUPPLIER),
        SeedUser("afhaler-a@mail.com", "Afhaler A", Role.COLLECTOR),
        SeedUser("afhaler-b@mail.com", "Afhaler B", Role.COLLECTOR),
    )

    // A supermarket, a bakery and a wholesaler in Breda, so the distance in search results means something.
    private val suppliers = listOf(
        SeedSupplier("supermarkt@mail.com", "Supermarkt Breda Centrum", "Grote Markt 5, Breda", 51.5890, 4.7760),
        SeedSupplier("bakker@mail.com", "Bakkerij De Korenschoof", "Ginnekenweg 12, Breda", 51.5795, 4.7820),
        SeedSupplier("groothandel@mail.com", "Groothandel Vers & Vries", "Minervum 7001, Breda", 51.6020, 4.7350),
    )

    private val products = listOf(
        // One LISTED product per discount tier (§9.4), all with an open pickup window (-2h to +6h).
        // FRESH: more than 48h (0%), up to 48h (10%), up to 24h (40%), up to 12h (60%).
        SeedProduct(
            key = "supermarkt-melk", supplierEmail = "supermarkt@mail.com", category = ProductCategory.FRESH,
            name = "Halfvolle melk 1 liter", priceCents = 249, quantity = 6,
            bestBefore = 72.hours, pickupFrom = (-2).hours, pickupUntil = 6.hours,
            allergens = setOf(Allergen.MILK),
        ),
        SeedProduct(
            key = "bakker-brood", supplierEmail = "bakker@mail.com", category = ProductCategory.FRESH,
            name = "Volkoren brood", priceCents = 349, quantity = 3,
            bestBefore = 30.hours, pickupFrom = (-2).hours, pickupUntil = 6.hours,
            allergens = setOf(Allergen.CEREALS_CONTAINING_GLUTEN),
        ),
        SeedProduct(
            key = "supermarkt-salade", supplierEmail = "supermarkt@mail.com", category = ProductCategory.FRESH,
            name = "Caesarsalade", priceCents = 399, quantity = 2,
            bestBefore = 20.hours, pickupFrom = (-2).hours, pickupUntil = 6.hours,
            allergens = setOf(Allergen.EGGS, Allergen.MILK, Allergen.CEREALS_CONTAINING_GLUTEN),
        ),
        // Also the maxPrice demo of Stefan: €5,00 originally, €2,00 after 60% discount.
        SeedProduct(
            key = "bakker-taart", supplierEmail = "bakker@mail.com", category = ProductCategory.FRESH,
            name = "Aardbeienvlaai", priceCents = 500,
            bestBefore = 10.hours, pickupFrom = (-2).hours, pickupUntil = 6.hours,
            allergens = setOf(Allergen.CEREALS_CONTAINING_GLUTEN, Allergen.EGGS, Allergen.MILK),
        ),
        // FROZEN: more than 7 days (0%), up to 7 days (15%), up to 24h (40%).
        SeedProduct(
            key = "supermarkt-pizza", supplierEmail = "supermarkt@mail.com", category = ProductCategory.FROZEN,
            name = "Pizza margherita", priceCents = 299, quantity = 4,
            bestBefore = 30.days, pickupFrom = (-2).hours, pickupUntil = 6.hours,
            allergens = setOf(Allergen.CEREALS_CONTAINING_GLUTEN, Allergen.MILK),
        ),
        SeedProduct(
            key = "groothandel-groente", supplierEmail = "groothandel@mail.com", category = ProductCategory.FROZEN,
            name = "Diepvries boerenkool 2,5 kg", priceCents = 450, quantity = 10,
            bestBefore = 5.days, pickupFrom = (-2).hours, pickupUntil = 6.hours,
        ),
        SeedProduct(
            key = "groothandel-vis", supplierEmail = "groothandel@mail.com", category = ProductCategory.FROZEN,
            name = "Kabeljauwfilet 1 kg", priceCents = 1299, quantity = 2,
            bestBefore = 20.hours, pickupFrom = (-2).hours, pickupUntil = 6.hours,
            allergens = setOf(Allergen.FISH),
        ),
        // AMBIENT: more than 14 days (0%), up to 14 days (15%), up to 7 days (40%).
        SeedProduct(
            key = "groothandel-rijst", supplierEmail = "groothandel@mail.com", category = ProductCategory.AMBIENT,
            name = "Basmatirijst 5 kg", priceCents = 899, quantity = 5,
            bestBefore = 60.days, pickupFrom = (-2).hours, pickupUntil = 6.hours,
        ),
        SeedProduct(
            key = "supermarkt-koekjes", supplierEmail = "supermarkt@mail.com", category = ProductCategory.AMBIENT,
            name = "Roomboterkoekjes", priceCents = 199, quantity = 8,
            bestBefore = 10.days, pickupFrom = (-2).hours, pickupUntil = 6.hours,
            allergens = setOf(Allergen.CEREALS_CONTAINING_GLUTEN, Allergen.MILK, Allergen.EGGS),
        ),
        SeedProduct(
            key = "bakker-beschuit", supplierEmail = "bakker@mail.com", category = ProductCategory.AMBIENT,
            name = "Beschuit", priceCents = 149, quantity = 4,
            bestBefore = 5.days, pickupFrom = (-2).hours, pickupUntil = 6.hours,
            allergens = setOf(Allergen.CEREALS_CONTAINING_GLUTEN),
        ),

        // Stefan, 422 on reserving: still fresh, but the pickup window has passed.
        SeedProduct(
            key = "groothandel-kaas", supplierEmail = "groothandel@mail.com", category = ProductCategory.FRESH,
            name = "Jonge kaas 1 kg", priceCents = 1099,
            bestBefore = 40.hours, pickupFrom = (-10).hours, pickupUntil = (-4).hours,
            allergens = setOf(Allergen.MILK),
        ),
        // Lonneke, status monitoring: LISTED but past its date; the first maintenance run expires it.
        SeedProduct(
            key = "supermarkt-yoghurt", supplierEmail = "supermarkt@mail.com", category = ProductCategory.FRESH,
            name = "Griekse yoghurt", priceCents = 179,
            bestBefore = (-3).hours, pickupFrom = (-10).hours, pickupUntil = (-4).hours,
            allergens = setOf(Allergen.MILK),
        ),

        // RESERVED: each has exactly one ACTIVE reservation below (§9.5).
        SeedProduct(
            key = "bakker-croissants", supplierEmail = "bakker@mail.com", category = ProductCategory.FRESH,
            name = "Roomboter croissants", priceCents = 450,
            bestBefore = 20.hours, pickupFrom = (-2).hours, pickupUntil = 8.hours,
            status = ProductStatus.RESERVED,
            allergens = setOf(Allergen.CEREALS_CONTAINING_GLUTEN, Allergen.MILK),
        ),
        SeedProduct(
            key = "supermarkt-kip", supplierEmail = "supermarkt@mail.com", category = ProductCategory.FRESH,
            name = "Kipfilet 500 g", priceCents = 899,
            bestBefore = 30.hours, pickupFrom = (-1).hours, pickupUntil = 10.hours,
            status = ProductStatus.RESERVED,
        ),
        SeedProduct(
            key = "groothandel-soep", supplierEmail = "groothandel@mail.com", category = ProductCategory.FRESH,
            name = "Verse tomatensoep 1 liter", priceCents = 600,
            bestBefore = 10.hours, pickupFrom = (-1).hours, pickupUntil = 4.hours,
            status = ProductStatus.RESERVED,
            allergens = setOf(Allergen.CELERY),
        ),
        SeedProduct(
            key = "supermarkt-wraps", supplierEmail = "supermarkt@mail.com", category = ProductCategory.FRESH,
            name = "Wraps kip-kerrie", priceCents = 399,
            bestBefore = 20.hours, pickupFrom = (-6).hours, pickupUntil = (-1).hours,
            status = ProductStatus.RESERVED,
            allergens = setOf(Allergen.CEREALS_CONTAINING_GLUTEN, Allergen.MUSTARD),
        ),
        SeedProduct(
            key = "groothandel-yoghurt", supplierEmail = "groothandel@mail.com", category = ProductCategory.FRESH,
            name = "Volle yoghurt 5 liter", priceCents = 250,
            bestBefore = 30.hours, pickupFrom = (-30).hours, pickupUntil = 5.hours,
            status = ProductStatus.RESERVED,
            allergens = setOf(Allergen.MILK),
        ),
        // Lonneke, status monitoring: RESERVED but past its date.
        SeedProduct(
            key = "supermarkt-brood", supplierEmail = "supermarkt@mail.com", category = ProductCategory.FRESH,
            name = "Wit casinobrood", priceCents = 300,
            bestBefore = (-2).hours, pickupFrom = (-8).hours, pickupUntil = (-3).hours,
            status = ProductStatus.RESERVED,
            allergens = setOf(Allergen.CEREALS_CONTAINING_GLUTEN),
        ),

        // End states.
        SeedProduct(
            key = "bakker-appeltaart", supplierEmail = "bakker@mail.com", category = ProductCategory.FRESH,
            name = "Appeltaart", priceCents = 650,
            bestBefore = 15.hours, pickupFrom = (-6).hours, pickupUntil = 2.hours,
            status = ProductStatus.COLLECTED,
            allergens = setOf(Allergen.CEREALS_CONTAINING_GLUTEN, Allergen.EGGS, Allergen.MILK),
        ),
        SeedProduct(
            key = "groothandel-melk", supplierEmail = "groothandel@mail.com", category = ProductCategory.FRESH,
            name = "Karnemelk 1 liter", priceCents = 129,
            bestBefore = (-24).hours, pickupFrom = (-48).hours, pickupUntil = (-30).hours,
            status = ProductStatus.EXPIRED,
            allergens = setOf(Allergen.MILK),
        ),
        // B-31: a removed product, so the admin overview can filter on status=REMOVED.
        SeedProduct(
            key = "supermarkt-sap", supplierEmail = "supermarkt@mail.com", category = ProductCategory.AMBIENT,
            name = "Appelsap 1,5 liter", priceCents = 219,
            bestBefore = 20.days, pickupFrom = (-2).hours, pickupUntil = 6.hours,
            status = ProductStatus.REMOVED,
        ),
    )

    // Each price is the original price minus the discount of the tier the product was in when it was reserved.
    private val reservations = listOf(
        // Collector A, live in the demo: one to cancel, one to collect. Both stay ACTIVE for hours.
        SeedReservation("bakker-croissants", "afhaler-a@mail.com", priceCents = 270, reservedAgo = 1.hours),
        SeedReservation("supermarkt-kip", "afhaler-a@mail.com", priceCents = 809, reservedAgo = 2.hours),
        // Collector B, fixed price: reserved 20 hours ago at 10%, the product is now at 60% (€5,40 against €2,40).
        SeedReservation("groothandel-soep", "afhaler-b@mail.com", priceCents = 540, reservedAgo = 20.hours),
        // lapseOverdue: one past its pickup window, one older than 24 hours.
        SeedReservation("supermarkt-wraps", "afhaler-a@mail.com", priceCents = 239, reservedAgo = 3.hours),
        SeedReservation("groothandel-yoghurt", "afhaler-b@mail.com", priceCents = 250, reservedAgo = 25.hours),
        SeedReservation("supermarkt-brood", "afhaler-a@mail.com", priceCents = 120, reservedAgo = 6.hours),
        // History of collector A. The COLLECTED one gets collected_at once issue #42 adds the column.
        SeedReservation(
            "bakker-appeltaart", "afhaler-a@mail.com", priceCents = 390, reservedAgo = 5.hours,
            status = ReservationStatus.COLLECTED,
        ),
        SeedReservation(
            "supermarkt-melk", "afhaler-a@mail.com", priceCents = 249, reservedAgo = 10.hours,
            status = ReservationStatus.CANCELLED,
        ),
        SeedReservation(
            "groothandel-groente", "afhaler-a@mail.com", priceCents = 382, reservedAgo = 30.hours,
            status = ReservationStatus.LAPSED,
        ),
    )

    // Demo only: public on purpose, because README.md and requests.http name it for every seed account.
    private const val DEMO_PASSWORD = "Demo-Restpartij-2026"

    /**
     * Fills the database when it has no users yet; otherwise it does nothing, so a restart in file mode adds no
     * duplicates. Everything goes in one transaction: a failure halfway leaves an empty database, not a half one.
     *
     * Not suspend: it runs once at startup, before the first request can arrive.
     *
     * @param clock the moment all seed dates are relative to; tests pass a fixed one.
     * @param hasher hashes the demo password, so /auth/login accepts the seed accounts.
     */
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