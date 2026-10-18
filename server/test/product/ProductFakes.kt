package com.restpartijen.api.product

import com.restpartijen.api.persistence.Repository
import com.restpartijen.api.product.model.SurplusProduct
import com.restpartijen.api.product.model.SurplusProduct.ProductFactory
import com.restpartijen.api.product.repository.ProductAllergensTable
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.Role
import com.restpartijen.api.shared.SupplierSummary
import com.restpartijen.api.testsupport.FakeRepository
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import com.restpartijen.api.testsupport.insertSupplier
import com.restpartijen.api.testsupport.insertUser
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * A ProductRepository for tests, backed by the generic FakeRepository (decision 6.4).
 *
 * Delegation (`by`) passes every Repository call on to the fake, so no repository logic is repeated here.
 * Once ProductRepository gets product-specific queries, this object has to implement those itself.
 */
fun fakeProductRepository(vararg products: SurplusProduct): ProductRepository =
    object : ProductRepository, Repository<SurplusProduct> by FakeRepository(
        idOf = { it.id },
        withId = { product, id -> product.withId(id) },
        initialItems = products.toList()
    ) {}

/** The moment the default dates of [aProduct] are based on; tests use it for their FixedClock. */
val testNow: Instant = Instant.parse("2026-10-01T00:00:00Z")

/**
 * Builds a valid product; a test passes only the values it is about.
 * Id 0 means "not stored yet": the database or fake chooses the real id.
 */
fun aProduct(
    category: ProductCategory = ProductCategory.FRESH,
    id: Long = 0,
    supplier: SupplierSummary = SupplierSummary(id = 1, name = "Bakkerij Test", latitude = 52.0, longitude = 5.0),
    name: String = "Volkoren brood",
    barcode: String? = null,
    originalPrice: Money = Money.fromCents(350),
    bestBeforeAt: Instant = testNow + 3.days,
    quantity: Int = 1,
    status: ProductStatus = ProductStatus.LISTED,
    pickupWindow: PickupWindow = PickupWindow(from = testNow, until = testNow + 2.days),
    allergens: Set<Allergen> = emptySet(),
): SurplusProduct = ProductFactory.create(
    category = category, id = id, name = name, barcode = barcode, originalPrice = originalPrice,
    supplier = supplier, bestBeforeAt = bestBeforeAt, quantity = quantity, status = status,
    pickupWindow = pickupWindow, allergens = allergens,
)

/** The same product with another id. SurplusProduct has no copy(), because it is not a data class. */
fun SurplusProduct.withId(id: Long): SurplusProduct = ProductFactory.create(
    category = category, id = id, name = name, barcode = barcode, originalPrice = originalPrice,
    supplier = supplier, bestBeforeAt = bestBeforeAt, quantity = quantity, status = status,
    pickupWindow = pickupWindow, allergens = allergens,
)

/**
 * All values of a product, for assertEquals.
 * SurplusProduct is not a data class, so equals() compares identity instead of content.
 */
fun SurplusProduct.fields(): List<Any?> = listOf(
    id, category, supplier, name, barcode, originalPrice, bestBeforeAt, quantity, status, pickupWindow, allergens
)

/**
 * Gives the calling test its own empty database with the F1 tables and one stored supplier.
 * A product must belong to a supplier (foreign key), so every database test of F1 starts here.
 */
fun emptyDatabaseWithSupplier(): SupplierSummary {
    createEmptyTestDatabase(UsersTable, SuppliersTable, ProductsTable, ProductAllergensTable)
    // A supplier must belong to an existing user (foreign key, issue #36).
    val id = insertSupplier(insertUser(Role.SUPPLIER))
    // The same values insertSupplier stores, so a test can compare a read product with this summary.
    return SupplierSummary(id = id, name = "Bakkerij Test", latitude = 51.59, longitude = 4.78)
}

/** Sets a status in the table itself: the repository always stores a new product as LISTED. */
fun setStatus(productId: Long, status: ProductStatus) {
    transaction {
        ProductsTable.update({ ProductsTable.id eq productId }) { it[ProductsTable.status] = status }
    }
}

/** Reads the status from the table itself, because findById hides a REMOVED product. */
fun statusOf(productId: Long): ProductStatus = transaction {
    ProductsTable.selectAll().where { ProductsTable.id eq productId }.single()[ProductsTable.status]
}
