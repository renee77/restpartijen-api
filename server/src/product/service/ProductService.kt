package com.restpartijen.api.product.service

import com.restpartijen.api.product.model.SurplusProduct
import com.restpartijen.api.product.model.SurplusProduct.ProductFactory
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.product.repository.SupplierRepository
import com.restpartijen.api.shared.DomainRuleException
import com.restpartijen.api.shared.ForbiddenException
import com.restpartijen.api.shared.IllegalStateTransitionException
import com.restpartijen.api.shared.NotFoundException
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.PriceProvider
import com.restpartijen.api.shared.ProductStatus
import kotlin.time.Clock

/**
 * The domain logic of F1: reading, placing and deleting surplus products; products are returned with their
 * price breakdown.
 *
 * @param repository stores and finds products
 * @param suppliers finds the supplier that belongs to the user in the token (US-01),
 *   and whether a supplier exists (US-03)
 * @param priceProvider gives the price breakdown of a product; F3 calculates it, F1 only asks (K-4, SD-3)
 * @param ownershipGuard checks that a supplier only changes or deletes an own product (US-03, §16.2)
 * @param clock decides what "now" is for the listing rules (GI-6, decision 6.5)
 */
class ProductService(
    private val repository: ProductRepository,
    private val suppliers: SupplierRepository,
    private val priceProvider: PriceProvider,
    private val ownershipGuard: OwnershipGuard,
    private val clock: Clock,
) {
    /**
     * The product with this [id] and its price breakdown (§5.7, SD-3).
     *
     * @throws NotFoundException when the id is unknown or the product is REMOVED (404, B-32)
     */
    suspend fun getProduct(id: Long): ProductDetail {
        val storedProduct = repository.findById(id) ?: throw NotFoundException("Product not found")

        return toDetail(storedProduct)
    }

    /**
     * Places a product for the supplier of [userId] (US-01, SD-1).
     *
     * @throws ForbiddenException when the user has no supplier (403)
     * @throws DomainRuleException when there is no pickup window, or a listing rule is broken (422)
     */
    suspend fun createProduct(userId: Long, command: PlaceProductCommand): ProductDetail {
        // The supplier comes from the token, never from the request (§16.2).
        val supplier = suppliers.findByUserId(userId)
            ?: throw ForbiddenException("This account has no supplier")

        val pickupWindow = requirePickupWindow(command.pickupWindow)

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
        // From here on only the stored product, which has the id the database chose.
        val storedProduct = repository.create(product)
        return toDetail(storedProduct)
    }

    /**
     * The current offer of [supplierId] for "Mijn aanbod": its LISTED and RESERVED products, each with its
     * price breakdown (US-03, A4). A supplier without products gets an empty list.
     *
     * @throws NotFoundException when no supplier with this id exists (404)
     */
    suspend fun getSupplierProducts(supplierId: Long): List<ProductDetail> {
        // Checked separately, so an unknown supplier (404) differs from one without products (empty list).
        if (!suppliers.exists(supplierId)) throw NotFoundException("Supplier not found")

        val productList = repository.findBySupplier(supplierId)

        // The repository already left out the stored end states. A LISTED product past its best-before
        // shows as EXPIRED until the status updater runs (SD-3), so that one is left out here.
        return productList.map { toDetail(it) }.filter { it.status != ProductStatus.EXPIRED }
    }

    /**
     * Deletes an own product of the supplier of [userId] (US-03). It is a soft delete: the product gets
     * status REMOVED and stays in the database (§9.5, B-19).
     *
     * @throws NotFoundException when the product does not exist or is already REMOVED (404, issue #45)
     * @throws ForbiddenException when the product belongs to another supplier, or the user has none (403)
     * @throws IllegalStateTransitionException when the product is not LISTED (409, §5.3)
     */
    suspend fun deleteProduct(userId: Long, productId: Long) {
        // First 404, then 403: without a product there is no owner to check.
        val product = repository.findById(productId) ?: throw NotFoundException("Product not found")

        ownershipGuard.requireOwner(userId, product)

        // The repository deletes only a LISTED product and checks that in the same statement, so a product
        // reserved after findById is not deleted after all (race). False means it is not LISTED.
        if (!repository.delete(product.id)) {
            throw IllegalStateTransitionException("Only a LISTED product can be deleted")
        }
    }

    /**
     * Replaces the changeable fields of an own product of the supplier of [userId] (US-03, A13).
     * Id, supplier, category and status stay as stored; every other field comes from [command].
     *
     * @throws NotFoundException when the product does not exist or is REMOVED (404)
     * @throws ForbiddenException when the product belongs to another supplier, or the user has none (403)
     * @throws IllegalStateTransitionException when the product is not LISTED (409)
     * @throws DomainRuleException when there is no pickup window, or a listing rule is broken (422)
     */
    suspend fun updateProduct(userId: Long, productId: Long, command: UpdateProductCommand): ProductDetail {
        // The same order as deleteProduct: first 404, then 403.
        val product = repository.findById(productId) ?: throw NotFoundException("Product not found")

        ownershipGuard.requireOwner(userId, product)

        // Before the listing rules (409 before 422): a product that may not change at all should not get a
        // message about its dates.
        if (product.status != ProductStatus.LISTED) {
            throw IllegalStateTransitionException("Only a LISTED product can be updated")
        }

        // The window of the request, not the stored one: PUT replaces it.
        val pickupWindow = requirePickupWindow(command.pickupWindow)

        // A13: id, supplier, category and status stay as stored; every other field comes from the command.
        val updatedProduct = ProductFactory.create(
            category = product.category,
            id = product.id,
            name = command.name,
            barcode = command.barcode,
            originalPrice = command.originalPrice,
            supplier = product.supplier,
            bestBeforeAt = command.bestBeforeAt,
            quantity = command.quantity,
            status = product.status,
            pickupWindow = pickupWindow,
            allergens = command.allergens,
        )

        // Before storing, so a product that breaks a rule never reaches the database (422).
        updatedProduct.validateForListing(clock)

        // The repository changes only a LISTED product, in the same statement. False means the product was
        // reserved after the check above (race).
        if (!repository.update(updatedProduct)) {
            throw IllegalStateTransitionException("Only a LISTED product can be updated")
        }
        return toDetail(updatedProduct)
    }

    // Shared by createProduct and updateProduct. Without a window, the API would derive one from the opening
    // hours (US-10). Those do not exist yet, so "no window and no opening hours" always applies (§5.1).
    private fun requirePickupWindow(window: PickupWindow?): PickupWindow =
        window ?: throw DomainRuleException(
            "pickupWindow is required: the supplier has no opening hours",
            field = "pickupWindow"
        )

    // Shared by every function that returns products, so the EXPIRED rule lives in one place.
    private fun toDetail(product: SurplusProduct): ProductDetail {
        val price = priceProvider.priceBreakdown(product)
        val status =
            if (price == null && product.status == ProductStatus.LISTED) ProductStatus.EXPIRED else product.status
        return ProductDetail(product, price, status)
    }
}
