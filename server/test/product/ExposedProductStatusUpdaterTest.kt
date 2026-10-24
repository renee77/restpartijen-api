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

    /** Stores the products the tests change; its own behaviour is covered by ExposedProductRepositoryTest. */
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

    @Test
    fun `markCollected marks a reserved product as collected`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val product = repository.create(aProduct(supplier = supplier))
        setStatus(product.id, ProductStatus.RESERVED)

        // Act
        val collected = updater.markCollected(product.id)

        // Assert
        assertTrue(collected)
        assertEquals(ProductStatus.COLLECTED, statusOf(product.id))
    }

    @Test
    fun `markCollected returns false and changes nothing for a product that is not reserved`() = runTest {
        // Arrange: a LISTED product was never reserved, so nobody can have collected it (§9.5)
        val supplier = emptyDatabaseWithSupplier()
        val product = repository.create(aProduct(supplier = supplier))

        // Act
        val collected = updater.markCollected(product.id)

        // Assert
        assertFalse(collected)
        assertEquals(ProductStatus.LISTED, statusOf(product.id))
    }

    @Test
    fun `markCollected returns false for an unknown id`() = runTest {
        // Arrange
        emptyDatabaseWithSupplier()

        // Act
        val collected = updater.markCollected(999)

        // Assert
        assertFalse(collected)
    }

    @Test
    fun `markRemoved removes a listed product`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val product = repository.create(aProduct(supplier = supplier))

        // Act
        val removed = updater.markRemoved(product.id)

        // Assert
        assertTrue(removed)
        assertEquals(ProductStatus.REMOVED, statusOf(product.id))
    }

    @Test
    fun `markRemoved returns false and changes nothing for a product that is not listed`() = runTest {
        // Arrange: a reserved product is held for a collector; the admin lapses the reservation first (US-09)
        val supplier = emptyDatabaseWithSupplier()
        val product = repository.create(aProduct(supplier = supplier))
        setStatus(product.id, ProductStatus.RESERVED)

        // Act
        val removed = updater.markRemoved(product.id)

        // Assert
        assertFalse(removed)
        assertEquals(ProductStatus.RESERVED, statusOf(product.id))
    }

    @Test
    fun `markRemoved returns false for an unknown id`() = runTest {
        // Arrange
        emptyDatabaseWithSupplier()

        // Act
        val removed = updater.markRemoved(999)

        // Assert
        assertFalse(removed)
    }

    @Test
    fun `markExpired expires only the listed products it is given and returns how many`() = runTest {
        // Arrange: two products in the list, and one that is not in it
        val supplier = emptyDatabaseWithSupplier()
        val first = repository.create(aProduct(supplier = supplier))
        val second = repository.create(aProduct(supplier = supplier))
        val notInList = repository.create(aProduct(supplier = supplier))

        // Act
        val expired = updater.markExpired(listOf(first.id, second.id))

        // Assert
        assertEquals(2, expired)
        assertEquals(ProductStatus.EXPIRED, statusOf(first.id))
        assertEquals(ProductStatus.EXPIRED, statusOf(second.id))
        assertEquals(ProductStatus.LISTED, statusOf(notInList.id))
    }

    @Test
    fun `markExpired skips a product that is no longer listed`() = runTest {
        // Arrange: reserved between findExpiredListings and markExpired, so it must stay reserved
        val supplier = emptyDatabaseWithSupplier()
        val listed = repository.create(aProduct(supplier = supplier))
        val reserved = repository.create(aProduct(supplier = supplier))
        setStatus(reserved.id, ProductStatus.RESERVED)

        // Act
        val expired = updater.markExpired(listOf(listed.id, reserved.id))

        // Assert: the count says what really changed
        assertEquals(1, expired)
        assertEquals(ProductStatus.RESERVED, statusOf(reserved.id))
    }

    @Test
    fun `markExpired returns 0 for an empty list`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val product = repository.create(aProduct(supplier = supplier))

        // Act
        val expired = updater.markExpired(emptyList())

        // Assert
        assertEquals(0, expired)
        assertEquals(ProductStatus.LISTED, statusOf(product.id))
    }
}
