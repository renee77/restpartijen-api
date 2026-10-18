package com.restpartijen.api.product

import com.restpartijen.api.persistence.Repository
import com.restpartijen.api.product.model.SurplusProduct
import com.restpartijen.api.product.model.SurplusProduct.ProductFactory
import com.restpartijen.api.product.repository.ProductAllergensTable
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.product.repository.ProductsTable
import com.restpartijen.api.product.repository.SuppliersTable
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.SupplierSummary
import com.restpartijen.api.testsupport.FakeRepository
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
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
    createEmptyTestDatabase(SuppliersTable, ProductsTable, ProductAllergensTable)
    val supplier = SupplierSummary(id = 0, name = "Bakkerij Test", latitude = 51.59, longitude = 4.78)
    val id = transaction {
        SuppliersTable.insert {
            it[userId] = 1
            it[name] = supplier.name
            it[address] = "Teststraat 1, Breda"
            it[latitude] = supplier.latitude
            it[longitude] = supplier.longitude
        }[SuppliersTable.id]
    }
    return supplier.copy(id = id)
}
