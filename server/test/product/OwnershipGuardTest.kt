package com.restpartijen.api.product

import com.restpartijen.api.product.service.OwnershipGuard
import com.restpartijen.api.shared.ForbiddenException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The ownership check of US-03 and US-10 (§16.2): a supplier may only change or delete an own product,
 * and only replace its own opening hours.
 * The role check (SUPPLIER) happens earlier, in the route; this is the second, separate step.
 */
class OwnershipGuardTest {

    /** The fake knows aSupplier, owned by user 7; any other user has no supplier. */
    private val ownerUserId = 7L
    private val guard = OwnershipGuard(fakeSupplierRepository(aSupplier, ownerUserId = ownerUserId))

    @Test
    fun `requireOwner accepts a product of the supplier of the user`() = runTest {
        // Arrange
        val product = aProduct(id = 1, supplier = aSupplier)

        // Act + Assert: no exception means the user may go on
        guard.requireOwner(userId = ownerUserId, product = product)
    }

    @Test
    fun `requireOwner refuses a product of another supplier`() = runTest {
        // Arrange
        val product = aProduct(id = 1, supplier = aSupplier.copy(id = 2))

        // Act + Assert
        assertFailsWith<ForbiddenException> {
            guard.requireOwner(userId = ownerUserId, product = product)
        }
    }

    @Test
    fun `requireOwner refuses a user without a supplier`() = runTest {
        // Arrange: the role check normally stops this user first; the guard does not rely on that
        val product = aProduct(id = 1, supplier = aSupplier)

        // Act + Assert
        assertFailsWith<ForbiddenException> {
            guard.requireOwner(userId = 8, product = product)
        }
    }

    // --- requireOwnSupplier (US-10): the same check for data that belongs to a supplier, not a product ---

    @Test
    fun `requireOwnSupplier accepts the supplier of the user`() = runTest {
        // Act + Assert: no exception means the user may go on
        guard.requireOwnSupplier(userId = ownerUserId, supplierId = aSupplier.id)
    }

    @Test
    fun `requireOwnSupplier refuses another supplier`() = runTest {
        // Act + Assert
        assertFailsWith<ForbiddenException> {
            guard.requireOwnSupplier(userId = ownerUserId, supplierId = aSupplier.id + 1)
        }
    }

    @Test
    fun `requireOwnSupplier refuses a user without a supplier`() = runTest {
        // Act + Assert
        assertFailsWith<ForbiddenException> {
            guard.requireOwnSupplier(userId = 8, supplierId = aSupplier.id)
        }
    }
}
