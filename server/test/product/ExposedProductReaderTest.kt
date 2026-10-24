package com.restpartijen.api.product

import com.restpartijen.api.product.repository.ExposedProductReader
import com.restpartijen.api.product.repository.ExposedProductRepository
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.testsupport.FixedClock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.days

/**
 * Integration test: the real ProductReader against a real, empty H2 database per test (§12.1).
 * One test per promise in the KDoc of ProductReader.
 */
class ExposedProductReaderTest {

    /** Stores the products the tests read; its own behaviour is covered by ExposedProductRepositoryTest. */
    private val repository = ExposedProductRepository(FixedClock(testNow))

    /** The reader passes findById on to the repository, so both promise exactly the same. */
    private val reader = ExposedProductReader(repository)

    @Test
    fun `findById returns the stored product`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val stored = repository.create(aProduct(supplier = supplier, name = "Volkoren brood"))

        // Act
        val found = reader.findById(stored.id)

        // Assert
        assertEquals(stored.id, found?.id)
        assertEquals("Volkoren brood", found?.name)
        assertEquals(supplier, found?.supplier)
    }

    @Test
    fun `findById returns null for an unknown id`() = runTest {
        // Arrange
        emptyDatabaseWithSupplier()

        // Act
        val found = reader.findById(999)

        // Assert
        assertNull(found)
    }

    @Test
    fun `findById returns null for a removed product`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        val stored = repository.create(aProduct(supplier = supplier))
        repository.delete(stored.id)

        // Act
        val found = reader.findById(stored.id)

        // Assert: a REMOVED product counts as not found (§9.5)
        assertNull(found)
    }

    @Test
    fun `findAvailable returns only listed products that are still good`() = runTest {
        // Arrange: one product that matches, and one for every reason to leave a product out
        val supplier = emptyDatabaseWithSupplier()
        val available = repository.create(aProduct(supplier = supplier, bestBeforeAt = testNow + 3.days))
        repository.create(aProduct(supplier = supplier, bestBeforeAt = testNow - 1.days))
        val reserved = repository.create(aProduct(supplier = supplier, bestBeforeAt = testNow + 3.days))
        setStatus(reserved.id, ProductStatus.RESERVED)
        val removed = repository.create(aProduct(supplier = supplier, bestBeforeAt = testNow + 3.days))
        repository.delete(removed.id)

        // Act
        val found = reader.findAvailable(testNow)

        // Assert
        assertEquals(listOf(available.id), found.map { it.id })
    }

    @Test
    fun `findAvailable leaves out a product whose best-before moment is exactly now`() = runTest {
        // Arrange: "after now" means a product at exactly now is no longer available
        val supplier = emptyDatabaseWithSupplier()
        repository.create(aProduct(supplier = supplier, bestBeforeAt = testNow))

        // Act
        val found = reader.findAvailable(testNow)

        // Assert
        assertEquals(emptyList(), found)
    }

    @Test
    fun `findExpiredListings returns only listed products whose best-before moment has passed`() = runTest {
        // Arrange: one product that matches, and one for every reason to leave a product out
        val supplier = emptyDatabaseWithSupplier()
        val expired = repository.create(aProduct(supplier = supplier, bestBeforeAt = testNow - 1.days))
        repository.create(aProduct(supplier = supplier, bestBeforeAt = testNow + 3.days))
        val reserved = repository.create(aProduct(supplier = supplier, bestBeforeAt = testNow - 1.days))
        setStatus(reserved.id, ProductStatus.RESERVED)
        val removed = repository.create(aProduct(supplier = supplier, bestBeforeAt = testNow - 1.days))
        repository.delete(removed.id)

        // Act
        val found = reader.findExpiredListings(testNow)

        // Assert
        assertEquals(listOf(expired.id), found.map { it.id })
    }

    @Test
    fun `findExpiredListings includes a product whose best-before moment is exactly now`() = runTest {
        // Arrange: "at or before now" is the mirror of findAvailable, so no product falls between the two
        val supplier = emptyDatabaseWithSupplier()
        val atNow = repository.create(aProduct(supplier = supplier, bestBeforeAt = testNow))

        // Act
        val found = reader.findExpiredListings(testNow)

        // Assert
        assertEquals(listOf(atNow.id), found.map { it.id })
    }

    @Test
    fun `findByStatus returns only products with one of the given statuses, including removed`() = runTest {
        // Arrange: one product per status that is asked for, and one that is not
        val supplier = emptyDatabaseWithSupplier()
        val reserved = repository.create(aProduct(supplier = supplier))
        setStatus(reserved.id, ProductStatus.RESERVED)
        val removed = repository.create(aProduct(supplier = supplier))
        repository.delete(removed.id)
        repository.create(aProduct(supplier = supplier))

        // Act
        val found = reader.findByStatus(setOf(ProductStatus.RESERVED, ProductStatus.REMOVED))

        // Assert: compared as a set, because the contract promises no order
        assertEquals(setOf(reserved.id, removed.id), found.map { it.id }.toSet())
    }

    @Test
    fun `findByStatus returns an empty list for an empty set`() = runTest {
        // Arrange
        val supplier = emptyDatabaseWithSupplier()
        repository.create(aProduct(supplier = supplier))

        // Act
        val found = reader.findByStatus(emptySet())

        // Assert
        assertEquals(emptyList(), found)
    }
}
