package com.restpartijen.api.product

import com.restpartijen.api.product.model.AmbientProduct
import com.restpartijen.api.product.model.FreshProduct
import com.restpartijen.api.product.model.FrozenProduct
import com.restpartijen.api.product.repository.ExposedProductRepository
import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.testsupport.FixedClock
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days

/**
 * Integration test: the real repository against a real, empty H2 database per test (E4).
 * Shows table, repository, transaction and test database working together.
 */
class ExposedProductRepositoryTest {

    private val repository = ExposedProductRepository(FixedClock(testNow))

    @Test
    fun `create stores the product and findById returns it`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        // Id 0 is never chosen by the database, so the id in the result proves it was generated.
        val newProduct = aProduct(supplier = supplier, barcode = "8710400000001", quantity = 4)

        // Act
        val product = repository.create(newProduct)

        // Assert
        assertNotEquals(0L, product.id)
        assertEquals(newProduct.withId(product.id).fields(), product.fields())
        assertEquals(product.fields(), repository.findById(product.id)?.fields())
    }

    @Test
    fun `create stores the allergens and findById returns them`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val allergens = setOf(Allergen.MILK, Allergen.CEREALS_CONTAINING_GLUTEN)

        // Act
        val product = repository.create(aProduct(supplier = supplier, allergens = allergens))

        // Assert
        assertEquals(allergens, repository.findById(product.id)?.allergens)
    }

    @Test
    fun `create stores the time of the clock as created_at`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()

        // Act
        val product = repository.create(aProduct(supplier = supplier))

        // Assert: read the table directly, because SurplusProduct has no createdAt
        val createdAt = transaction {
            ProductsTable.selectAll()
                .where { ProductsTable.id eq product.id }
                .single()[ProductsTable.createdAt]
        }
        assertEquals(testNow, createdAt)
    }

    @Test
    fun `findById returns the subclass that matches the stored category`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val fresh = repository.create(aProduct(supplier = supplier, category = ProductCategory.FRESH))
        val frozen = repository.create(aProduct(supplier = supplier, category = ProductCategory.FROZEN))
        val ambient = repository.create(aProduct(supplier = supplier, category = ProductCategory.AMBIENT))

        // Act
        val kinds = listOf(fresh, frozen, ambient).map { repository.findById(it.id)!!::class }

        // Assert
        assertEquals(listOf(FreshProduct::class, FrozenProduct::class, AmbientProduct::class), kinds)
    }

    @Test
    fun `findById returns null when the id does not exist`() = runTest {
        // Arrange
        emptyDatabaseWithSupplier()

        // Act
        val product = repository.findById(1L)

        // Assert
        assertNull(product)
    }

    @Test
    fun `findAll returns all stored products with their own allergens`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        // create() is part of the Arrange here: it only fills the database for findAll.
        val bread = repository.create(aProduct(supplier = supplier, allergens = setOf(Allergen.CEREALS_CONTAINING_GLUTEN)))
        val spinach = repository.create(
            aProduct(supplier = supplier, category = ProductCategory.FROZEN, name = "Diepvries spinazie")
        )

        // Act
        val products = repository.findAll()

        // Assert
        assertEquals(listOf(bread.fields(), spinach.fields()), products.map { it.fields() })
    }

    @Test
    fun `findAll returns an empty list when there are no products`() = runTest {
        // Arrange
        emptyDatabaseWithSupplier()

        // Act
        val products = repository.findAll()

        // Assert
        assertEquals(emptyList(), products)
    }

    @Test
    fun `update replaces only the product with the same id and returns true`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val bread = repository.create(aProduct(supplier = supplier))
        val spinach = repository.create(
            aProduct(supplier = supplier, category = ProductCategory.FROZEN, name = "Diepvries spinazie")
        )
        val changedSpinach = aProduct(
            id = spinach.id,
            supplier = supplier,
            category = ProductCategory.FROZEN,
            name = "Diepvries boerenkool",
            barcode = "8710400000002",
            originalPrice = Money.fromCents(499),
            bestBeforeAt = testNow + 60.days,
            quantity = 6,
            pickupWindow = PickupWindow(from = testNow + 1.days, until = testNow + 5.days),
        )

        // Act
        val updateSucceeded = repository.update(changedSpinach)

        // Assert
        assertTrue(updateSucceeded)
        assertEquals(listOf(bread.fields(), changedSpinach.fields()), repository.findAll().map { it.fields() })
    }

    @Test
    fun `update replaces the allergens of the product`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val bread = repository.create(aProduct(supplier = supplier, allergens = setOf(Allergen.MILK)))
        val newAllergens = setOf(Allergen.CEREALS_CONTAINING_GLUTEN, Allergen.SESAME)

        // Act
        repository.update(aProduct(id = bread.id, supplier = supplier, allergens = newAllergens))

        // Assert
        assertEquals(newAllergens, repository.findById(bread.id)?.allergens)
    }

    @Test
    fun `update leaves the status unchanged`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val bread = repository.create(aProduct(supplier = supplier))

        // Act: status changes only go through ProductStatusUpdater, so update ignores this one
        repository.update(aProduct(id = bread.id, supplier = supplier, status = ProductStatus.RESERVED))

        // Assert
        assertEquals(ProductStatus.LISTED, repository.findById(bread.id)?.status)
    }

    @Test
    fun `update returns false and changes nothing when the id does not exist`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val bread = repository.create(aProduct(supplier = supplier))

        // Act
        val updateSucceeded = repository.update(aProduct(id = 999, supplier = supplier, name = "Wit brood"))

        // Assert
        assertFalse(updateSucceeded)
        assertEquals(listOf(bread.fields()), repository.findAll().map { it.fields() })
    }

    @Test
    fun `delete hides only the product with the given id and returns true`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val bread = repository.create(aProduct(supplier = supplier))
        val spinach = repository.create(
            aProduct(supplier = supplier, category = ProductCategory.FROZEN, name = "Diepvries spinazie")
        )

        // Act
        val deleteSucceeded = repository.delete(bread.id)

        // Assert
        assertTrue(deleteSucceeded)
        assertNull(repository.findById(bread.id))
        assertEquals(listOf(spinach.fields()), repository.findAll().map { it.fields() })
    }

    @Test
    fun `delete returns false when the id does not exist`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val bread = repository.create(aProduct(supplier = supplier))

        // Act
        val deleteSucceeded = repository.delete(999)

        // Assert
        assertFalse(deleteSucceeded)
        assertEquals(listOf(bread.fields()), repository.findAll().map { it.fields() })
    }

    @Test
    fun `delete keeps the row and sets its status to REMOVED`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val bread = repository.create(aProduct(supplier = supplier))

        // Act
        repository.delete(bread.id)

        // Assert: read the table directly, past the repository that hides REMOVED rows
        val status = transaction {
            ProductsTable.selectAll()
                .where { ProductsTable.id eq bread.id }
                .single()[ProductsTable.status]
        }
        assertEquals(ProductStatus.REMOVED, status)
    }

    @Test
    fun `delete returns false the second time for the same id`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val bread = repository.create(aProduct(supplier = supplier))
        repository.delete(bread.id)

        // Act
        val secondDelete = repository.delete(bread.id)

        // Assert
        assertFalse(secondDelete)
    }

    // FTD §5.3 and §9.5: only LISTED may become REMOVED. A reserved product would otherwise vanish
    // while its reservation stays ACTIVE, and the end states COLLECTED and EXPIRED could still change.
    @Test
    fun `delete refuses a RESERVED product and leaves its status`() = runTest {
        assertDeleteRefused(ProductStatus.RESERVED)
    }

    @Test
    fun `delete refuses a COLLECTED product and leaves its status`() = runTest {
        assertDeleteRefused(ProductStatus.COLLECTED)
    }

    @Test
    fun `delete refuses an EXPIRED product and leaves its status`() = runTest {
        assertDeleteRefused(ProductStatus.EXPIRED)
    }

    private suspend fun assertDeleteRefused(status: ProductStatus) {
        // Arrange: create always stores LISTED, so the status is set in the table directly
        val supplier = emptyDatabaseWithSupplier()
        val bread = repository.create(aProduct(supplier = supplier))
        transaction { ProductsTable.update({ ProductsTable.id eq bread.id }) { it[ProductsTable.status] = status } }

        // Act
        val deleteSucceeded = repository.delete(bread.id)

        // Assert
        assertFalse(deleteSucceeded)
        val statusAfter = transaction {
            ProductsTable.selectAll().where { ProductsTable.id eq bread.id }.single()[ProductsTable.status]
        }
        assertEquals(status, statusAfter)
    }

    @Test
    fun `update returns false for a deleted product`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val bread = repository.create(aProduct(supplier = supplier))
        repository.delete(bread.id)

        // Act
        val updateSucceeded = repository.update(aProduct(id = bread.id, supplier = supplier, name = "Wit brood"))

        // Assert
        assertFalse(updateSucceeded)
    }
}
