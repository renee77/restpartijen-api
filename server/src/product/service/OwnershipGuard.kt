package com.restpartijen.api.product.service

import com.restpartijen.api.product.model.SurplusProduct
import com.restpartijen.api.product.repository.SupplierRepository
import com.restpartijen.api.shared.ForbiddenException

/**
 * The ownership check of F1 (§16.2): a supplier may only change or delete an own product (US-03).
 *
 * This is the second, separate step after the role check in the route. The service first finds the
 * product (404); this guard then compares its supplier with the supplier of the user in the token.
 *
 * @param suppliers finds the supplier that belongs to the user in the token
 */
class OwnershipGuard(private val suppliers: SupplierRepository) {

    /**
     * Returns normally when [product] belongs to the supplier of [userId].
     *
     * @throws ForbiddenException when the user has no supplier, or the product belongs to another supplier (403)
     */
    suspend fun requireOwner(userId: Long, product: SurplusProduct) {
        val supplier = suppliers.findByUserId(userId)
            ?: throw ForbiddenException("This account has no supplier")

        // Says what is wrong without revealing who the owner is.
        if (supplier.id != product.supplier.id) {
            throw ForbiddenException("This product belongs to another supplier")
        }
    }
}
