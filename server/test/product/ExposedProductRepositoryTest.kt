package com.restpartijen.api.product

import com.restpartijen.api.product.model.Product
import com.restpartijen.api.product.repository.ExposedProductRepository
import com.restpartijen.api.product.repository.ProductsTable
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration test: the real repository against a real, empty H2 database per test (E4).
 * Shows table, repository, transaction and test database working together.
 */
class ExposedProductRepositoryTest {

    @Test
    fun `create stores the product and findById returns it`() = runTest {
        // Arrange
        createEmptyTestDatabase(ProductsTable)
        val repository = ExposedProductRepository()
        // Id 0 is never chosen by the database, so the id in the result proves it was generated.
        val newProduct = Product(id = 0, name = "Volkoren brood", category = "FRESH")

        // Act
        val product = repository.create(newProduct)

        // Assert
        assertNotEquals(0L, product.id)
        assertEquals(product, repository.findById(product.id))
    }

    @Test
    fun `findById returns null when the id does not exist`() = runTest {
        // Arrange
        createEmptyTestDatabase(ProductsTable)
        val repository = ExposedProductRepository()

        // Act
        val product = repository.findById(1L)

        // Assert
        assertNull(product)
    }

    @Test
    fun `findAll returns all stored products`() = runTest {
        // Arrange
        createEmptyTestDatabase(ProductsTable)
        val repository = ExposedProductRepository()
        // create() is part of the Arrange here: it only fills the database for findAll.
        val bread = repository.create(Product(id = 0, name = "Volkoren brood", category = "FRESH"))
        val spinach = repository.create(Product(id = 0, name = "Diepvries spinazie", category = "FROZEN"))

        // Act
        val products = repository.findAll()

        // Assert
        assertEquals(listOf(bread, spinach), products)
    }

    @Test
    fun `findAll returns an empty list when there are no products`() = runTest {
        // Arrange
        createEmptyTestDatabase(ProductsTable)
        val repository = ExposedProductRepository()

        // Act
        val products = repository.findAll()

        // Assert
        assertEquals(emptyList(), products)
    }

    @Test
    fun `update replaces only the product with the same id and returns true`() = runTest {
        // Arrange
        createEmptyTestDatabase(ProductsTable)
        val repository = ExposedProductRepository()
        val bread = repository.create(Product(id = 0, name = "Volkoren brood", category = "FRESH"))
        val spinach = repository.create(Product(id = 0, name = "Diepvries spinazie", category = "FROZEN"))
        val changedSpinach = spinach.copy(name = "Diepvries boerenkool")

        // Act
        val updateSucceeded = repository.update(changedSpinach)

        // Assert
        assertTrue(updateSucceeded)
        assertEquals(listOf(bread, changedSpinach), repository.findAll())
    }

    @Test
    fun `update returns false and changes nothing when the id does not exist`() = runTest {
        // Arrange
        createEmptyTestDatabase(ProductsTable)
        val repository = ExposedProductRepository()
        val bread = repository.create(Product(id = 0, name = "Volkoren brood", category = "FRESH"))

        // Act
        val updateSucceeded = repository.update(Product(id = 999, name = "Wit brood", category = "FRESH"))

        // Assert
        assertFalse(updateSucceeded)
        assertEquals(listOf(bread), repository.findAll())
    }

    @Test
    fun `delete hides only the product with the given id and returns true`() = runTest {
        // Arrange
        createEmptyTestDatabase(ProductsTable)
        val repository = ExposedProductRepository()
        val bread = repository.create(Product(id = 0, name = "Volkoren brood", category = "FRESH"))
        val spinach = repository.create(Product(id = 0, name = "Diepvries spinazie", category = "FROZEN"))

        // Act
        val deleteSucceeded = repository.delete(bread.id)

        // Assert
        assertTrue(deleteSucceeded)
        assertNull(repository.findById(bread.id))
        assertEquals(listOf(spinach), repository.findAll())
    }

    @Test
    fun `delete returns false when the id does not exist`() = runTest {
        // Arrange
        createEmptyTestDatabase(ProductsTable)
        val repository = ExposedProductRepository()
        val bread = repository.create(Product(id = 0, name = "Volkoren brood", category = "FRESH"))

        // Act
        val deleteSucceeded = repository.delete(999)

        // Assert
        assertFalse(deleteSucceeded)
        assertEquals(listOf(bread), repository.findAll())
    }

    @Test
    fun `delete keeps the row and sets its status to REMOVED`() = runTest {
        // Arrange
        createEmptyTestDatabase(ProductsTable)
        val repository = ExposedProductRepository()
        val bread = repository.create(Product(id = 0, name = "Volkoren brood", category = "FRESH"))

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
        createEmptyTestDatabase(ProductsTable)
        val repository = ExposedProductRepository()
        val bread = repository.create(Product(id = 0, name = "Volkoren brood", category = "FRESH"))
        repository.delete(bread.id)

        // Act
        val secondDelete = repository.delete(bread.id)

        // Assert
        assertFalse(secondDelete)
    }

    @Test
    fun `update returns false for a deleted product`() = runTest {
        // Arrange
        createEmptyTestDatabase(ProductsTable)
        val repository = ExposedProductRepository()
        val bread = repository.create(Product(id = 0, name = "Volkoren brood", category = "FRESH"))
        repository.delete(bread.id)

        // Act
        val updateSucceeded = repository.update(bread.copy(name = "Wit brood"))

        // Assert
        assertFalse(updateSucceeded)
    }
}
