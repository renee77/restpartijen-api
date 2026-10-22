package com.restpartijen.api.product

import com.restpartijen.api.persistence.Repository
import com.restpartijen.api.product.model.SurplusProduct
import com.restpartijen.api.product.model.SurplusProduct.ProductFactory
import com.restpartijen.api.product.repository.ProductAllergensTable
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.product.repository.SupplierRepository
import com.restpartijen.api.product.client.OpenFoodFactsClient
import com.restpartijen.api.product.service.NotFoundCache
import com.restpartijen.api.product.service.OpenFoodFactsRateLimiter
import com.restpartijen.api.product.service.OwnershipGuard
import com.restpartijen.api.product.service.ProductLookupCache
import com.restpartijen.api.product.service.ProductService
import io.ktor.client.engine.mock.MockEngine
import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.PriceBreakdown
import com.restpartijen.api.shared.PriceProvider
import com.restpartijen.api.shared.PricedProduct
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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * A ProductRepository for tests, backed by the generic FakeRepository (decision 6.4).
 *
 * Delegation (`by`) passes every Repository call on to the fake, so no repository logic is repeated here.
 * The product-specific queries of ProductRepository are implemented here on top of findAll.
 */
fun fakeProductRepository(vararg products: SurplusProduct): ProductRepository {
    val store = FakeRepository(
        idOf = { it.id },
        withId = { product, id -> product.withId(id) },
        initialItems = products.toList()
    )
    return object : ProductRepository, Repository<SurplusProduct> by store {
        // The same rule as the real query (A4): only the current offer of this supplier.
        override suspend fun findBySupplier(supplierId: Long): List<SurplusProduct> =
            findAll().filter {
                it.supplier.id == supplierId && it.status in setOf(ProductStatus.LISTED, ProductStatus.RESERVED)
            }

        // The same rule as the real delete (§5.3): only a LISTED product can be deleted, otherwise false.
        // Removing it from the list matches what callers see of the soft delete: findById no longer finds it.
        override suspend fun delete(id: Long): Boolean =
            store.findById(id)?.status == ProductStatus.LISTED && store.delete(id)

        // The same rule as the real update (A13): only a LISTED product changes, otherwise false.
        override suspend fun update(item: SurplusProduct): Boolean =
            store.findById(item.id)?.status == ProductStatus.LISTED && store.update(item)
    }
}

/**
 * Builds a ProductService the way Dependencies.kt wires it: the OwnershipGuard uses the same
 * supplier repository as the service, so one fake decides both who the supplier is and who owns what.
 */
fun productService(
    repository: ProductRepository,
    suppliers: SupplierRepository,
    priceProvider: PriceProvider,
    clock: Clock,
    openFoodFacts: OpenFoodFactsClient = openFoodFactsNotCalled(),
): ProductService = ProductService(
    repository, suppliers, priceProvider, OwnershipGuard(suppliers), clock, openFoodFacts,
    ProductLookupCache(clock), NotFoundCache(clock), OpenFoodFactsRateLimiter(clock),
)

/**
 * An OpenFoodFactsClient for tests that do not look up a barcode.
 * A call fails loudly with an IllegalStateException, which the client does not turn into Unavailable.
 */
fun openFoodFactsNotCalled(): OpenFoodFactsClient =
    OpenFoodFactsClient(MockEngine { error("This test does not expect a call to Open Food Facts") })

/** The supplier the product fakes use by default; the same values as the default supplier of [aProduct]. */
val aSupplier = SupplierSummary(id = 1, name = "Bakkerij Test", latitude = 52.0, longitude = 5.0)

/**
 * A SupplierRepository for tests that knows one supplier, or none.
 * [ownerUserId] is the account the supplier belongs to; any other user has no supplier.
 */
fun fakeSupplierRepository(supplier: SupplierSummary? = aSupplier, ownerUserId: Long = 7): SupplierRepository =
    object : SupplierRepository {
        override suspend fun findByUserId(userId: Long): SupplierSummary? = supplier.takeIf { userId == ownerUserId }
        override suspend fun exists(supplierId: Long): Boolean = supplier?.id == supplierId
        override suspend fun lookupSupplierId(userId: Long): Long? = findByUserId(userId)?.id
    }

/** A price breakdown with a real discount: 3.50 with 40% off is 2.10 (§5.7). */
val aBreakdown = PriceBreakdown(
    originalPrice = Money.fromCents(350), discountPercentage = 40, currentPrice = Money.fromCents(210),
)

/**
 * A PriceProvider for tests that gives every product the same [breakdown].
 * Null means "expired", as in the real contract (B-18); the pricing rules themselves are F3's to test.
 */
class FixedPriceProvider(private val breakdown: PriceBreakdown? = aBreakdown) : PriceProvider {
    override fun currentPrice(product: PricedProduct): Money? = breakdown?.currentPrice
    override fun priceBreakdown(product: PricedProduct): PriceBreakdown? = breakdown
}

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
