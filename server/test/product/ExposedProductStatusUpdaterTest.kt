package com.restpartijen.api.product

import com.restpartijen.api.product.repository.ExposedProductRepository
import com.restpartijen.api.product.repository.ExposedProductStatusUpdater
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.testsupport.FixedClock
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration test: the real ProductStatusUpdater against a real, empty H2 database per test (§12.1).
 * One test per promise in the KDoc of ProductStatusUpdater.
 */
class ExposedProductStatusUpdaterTest {

    private val updater = ExposedProductStatusUpdater()

    // Stores the products the tests change; its own behaviour is covered by ExposedProductRepositoryTest.
    private val repository = ExposedProductRepository(FixedClock(testNow))

    @Test
    fun `markReserved reserves a listed product`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val product = repository.create(aProduct(supplier = supplier))

        // Act
        val reserved = updater.markReserved(product.id)

        // Assert
        assertTrue(reserved)
        assertEquals(ProductStatus.RESERVED, statusOf(product.id))
    }

    @Test
    fun `markReserved returns false and changes nothing for a product that is not listed`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val product = repository.create(aProduct(supplier = supplier))
        setStatus(product.id, ProductStatus.COLLECTED)

        // Act
        val reserved = updater.markReserved(product.id)

        // Assert
        assertFalse(reserved)
        assertEquals(ProductStatus.COLLECTED, statusOf(product.id))
    }

    @Test
    fun `markReserved returns false for an unknown id`() = runTest {
        // Arrange
        emptyDatabaseWithSupplier()

        // Act
        val reserved = updater.markReserved(999)

        // Assert
        assertFalse(reserved)
    }

    @Test
    fun `markReserved lets only one of two simultaneous callers win`() = runTest {
        // Arrange: a race is not guaranteed to show up in one try, so the test runs it on several products
        val supplier = emptyDatabaseWithSupplier()
        val products = List(20) { repository.create(aProduct(supplier = supplier)) }

        for (product in products) {
            // Act: two callers at the same time; dbQuery moves each to its own thread on Dispatchers.IO
            val results = listOf(
                async { updater.markReserved(product.id) },
                async { updater.markReserved(product.id) },
            ).awaitAll()

            // Assert: exactly one caller got the product (§5.5)
            assertEquals(1, results.count { it }, "product ${product.id}")
        }
    }

    @Test
    fun `markListed puts a reserved product back on the list`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val product = repository.create(aProduct(supplier = supplier))
        setStatus(product.id, ProductStatus.RESERVED)

        // Act
        val listed = updater.markListed(product.id)

        // Assert
        assertTrue(listed)
        assertEquals(ProductStatus.LISTED, statusOf(product.id))
    }

    @Test
    fun `markListed returns false and changes nothing for a product that is not reserved`() = runTest {
        // Arrange: COLLECTED is an end state, so it must never go back to LISTED (§9.5)
        val supplier = emptyDatabaseWithSupplier()
        val product = repository.create(aProduct(supplier = supplier))
        setStatus(product.id, ProductStatus.COLLECTED)

        // Act
        val listed = updater.markListed(product.id)

        // Assert
        assertFalse(listed)
        assertEquals(ProductStatus.COLLECTED, statusOf(product.id))
    }

    @Test
    fun `markListed returns false for an unknown id`() = runTest {
        // Arrange
        emptyDatabaseWithSupplier()

        // Act
        val listed = updater.markListed(999)

        // Assert
        assertFalse(listed)
    }
}
