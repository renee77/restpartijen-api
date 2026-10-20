package com.restpartijen.api.product.service

import com.restpartijen.api.product.model.SurplusProduct
import com.restpartijen.api.product.model.SurplusProduct.ProductFactory
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.product.repository.SupplierRepository
import com.restpartijen.api.shared.DomainRuleException
import com.restpartijen.api.shared.ForbiddenException
import com.restpartijen.api.shared.NotFoundException
import com.restpartijen.api.shared.ProductStatus
import kotlin.time.Clock

/**
 * The domain logic of F1: reading and placing surplus products.
 *
 * @param repository stores and finds products
 * @param suppliers finds the supplier that belongs to the user in the token (US-01)
 * @param clock decides what "now" is for the listing rules (GI-6, decision 6.5)
 */
class ProductService(
    private val repository: ProductRepository,
    private val suppliers: SupplierRepository,
    private val clock: Clock,
) {
    suspend fun getProduct(id: Long): SurplusProduct {
        return repository.findById(id) ?: throw NotFoundException("Product not found")
    }

    /**
     * Places a product for the supplier of [userId] (US-01, SD-1).
     *
     * @throws ForbiddenException when the user has no supplier (403)
     * @throws DomainRuleException when there is no pickup window, or a listing rule is broken (422)
     */
    suspend fun createProduct(userId: Long, command: PlaceProductCommand): SurplusProduct {
        // The supplier comes from the token, never from the request (§16.2).
        val supplier = suppliers.findByUserId(userId)
            ?: throw ForbiddenException("This account has no supplier")

        // Without a window, the API would derive one from the opening hours (US-10). Those do not exist yet,
        // so "no window and no opening hours" always applies (§5.1).
        val pickupWindow = command.pickupWindow
            ?: throw DomainRuleException("pickupWindow is required: the supplier has no opening hours", field = "pickupWindow")

        // Id 0: the database chooses the real id. A new product is always LISTED.
        val product = ProductFactory.create(
            category = command.category,
            id = 0,
            name = command.name,
            barcode = command.barcode,
            originalPrice = command.originalPrice,
            supplier = supplier,
            bestBeforeAt = command.bestBeforeAt,
            quantity = command.quantity,
            status = ProductStatus.LISTED,
            pickupWindow = pickupWindow,
            allergens = command.allergens,
        )

        // Before storing, so a product that breaks a rule never reaches the database.
        product.validateForListing(clock)
        return repository.create(product)
    }
}
