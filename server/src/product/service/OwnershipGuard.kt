package com.restpartijen.api.product.service

import com.restpartijen.api.product.model.SurplusProduct
import com.restpartijen.api.product.repository.SupplierRepository
import com.restpartijen.api.shared.ForbiddenException

/**
 * The ownership check of F1 (§16.2): a supplier may only change or delete an own product (US-03)
 * and only replace its own opening hours (US-10).
 *
 * This is the second, separate step after the role check in the route. The service first finds the
 * product (404); this guard then compares its supplier with the supplier of the user in the token.
 *
 * @property suppliers finds the supplier that belongs to the user in the token
 */
class OwnershipGuard(private val suppliers: SupplierRepository) {

    /**
     * Returns normally when [product] belongs to the supplier of [userId].
     *
     * @throws ForbiddenException when the user has no supplier, or the product belongs to another supplier (403)
     */
    suspend fun requireOwner(userId: Long, product: SurplusProduct) {
        // Says what is wrong without revealing who the owner is.
        if (supplierIdOf(userId) != product.supplier.id) {
            throw ForbiddenException("This product belongs to another supplier")
        }
    }

    /**
     * Returns normally when [supplierId] is the supplier of [userId]; for data of a supplier itself,
     * such as its opening hours (US-10).
     *
     * @throws ForbiddenException when the user has no supplier, or [supplierId] is another supplier (403)
     */
    suspend fun requireOwnSupplier(userId: Long, supplierId: Long) {
        if (supplierIdOf(userId) != supplierId) {
            throw ForbiddenException("These opening hours belong to another supplier")
        }
    }

    /**
     * The id of the supplier of [userId].
     * Shared by both checks, so "a user without a supplier gets 403" lives in one place.
     *
     * @throws ForbiddenException when the user has no supplier (403)
     */
    private suspend fun supplierIdOf(userId: Long): Long =
        suppliers.findByUserId(userId)?.id ?: throw ForbiddenException("This account has no supplier")
}
