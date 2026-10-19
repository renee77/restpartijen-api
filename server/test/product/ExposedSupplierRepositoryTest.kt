package com.restpartijen.api.product

import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.product.repository.ExposedSupplierRepository
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import com.restpartijen.api.testsupport.insertSupplier
import com.restpartijen.api.testsupport.insertUser
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Integration test: the real SupplierIdLookup of F1 against a real, empty H2 database per test (A15).
 * One test per promise in the KDoc of SupplierIdLookup.
 */
class ExposedSupplierRepositoryTest {

    private val repository = ExposedSupplierRepository()

    @Test
    fun `lookupSupplierId returns the id of the supplier of that user`() = runTest {
        // Arrange: a collector first, so the user id (2) differs from the supplier id (1).
        // With equal ids, a query on the wrong column would still give the right answer.
        createEmptyTestDatabase(UsersTable, SuppliersTable)
        insertUser(Role.COLLECTOR)
        val userId = insertUser(Role.SUPPLIER)
        val supplierId = insertSupplier(userId)

        // Act
        val found = repository.lookupSupplierId(userId)

        // Assert
        assertEquals(supplierId, found)
    }

    @Test
    fun `lookupSupplierId returns null for a user without a supplier`() = runTest {
        // Arrange: the collector (user 1) has no supplier row, but supplier 1 exists for another user,
        // so a query on the supplier id instead of the user id would wrongly find it.
        createEmptyTestDatabase(UsersTable, SuppliersTable)
        val userId = insertUser(Role.COLLECTOR)
        insertSupplier(insertUser(Role.SUPPLIER))

        // Act
        val found = repository.lookupSupplierId(userId)

        // Assert
        assertNull(found)
    }

    @Test
    fun `lookupSupplierId returns null for an unknown user id`() = runTest {
        // Arrange
        createEmptyTestDatabase(UsersTable, SuppliersTable)

        // Act
        val found = repository.lookupSupplierId(999)

        // Assert
        assertNull(found)
    }
}
