package com.restpartijen.api.product.model

import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.DomainRuleException
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.ProductView
import com.restpartijen.api.shared.SupplierSummary
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/** The 422 message for a best-before moment that has passed; ProductService gives the same one (US-10). */
const val BEST_BEFORE_PASSED = "bestBeforeAt must be in the future"

/**
 * A surplus product a supplier offers (§8.4).
 *
 * Sealed, so the compiler checks that the factory handles every kind. What is the same
 * for every kind lives here; each subclass only supplies its [category] and [maxShelfLife].
 * Not a data class: copy() would bypass the private setter on [status].
 *
 * @property barcode the barcode on the package, or null for a product without one, such as bread from a bakery
 * @property bestBeforeAt the moment the best-before date ends; from then on the product has expired
 * @property quantity how many units are offered; at least 1 when listed
 * @property allergens the EU allergens the product contains; empty when none were entered
 * @param status the lifecycle status of the product, as stored or LISTED for a new one
 */
sealed class SurplusProduct(
    override val id: Long,
    override val supplier: SupplierSummary,
    override val name: String,
    val barcode: String?,
    override val originalPrice: Money,
    val bestBeforeAt: Instant,
    val quantity: Int,
    status: ProductStatus,
    override val pickupWindow: PickupWindow,
    val allergens: Set<Allergen>,
) : ProductView {

    /**
     * The lifecycle status this product was built with.
     *
     * Final, because Kotlin does not allow a private setter on an open property.
     * Other features change the status only through ProductStatusUpdater.
     */
    final override var status: ProductStatus = status
        private set

    /** Each subclass returns its own kind (§9.3). */
    abstract override val category: ProductCategory

    /** Longest shelf life that is still plausible for this kind when listing (§9.10). */
    abstract val maxShelfLife: Duration

    final override fun shelfLifeRemaining(clock: Clock): Duration = bestBeforeAt - clock.now()

    /** True once best-before has been reached. */
    fun isExpired(clock: Clock): Boolean = shelfLifeRemaining(clock) <= Duration.ZERO

    /**
     * Checks the rules a product must meet when it is listed (§9.10).
     * The first three hold for every kind; the shelf-life limit comes from the subclass.
     *
     * Through the API, the first two never fail here: ProductService derives the pickup window first, and the
     * calculator already refuses an expired product and ends the window at bestBeforeAt at the latest (US-10).
     * They stay as rules of the product itself, for every other way a product is built.
     *
     * @throws DomainRuleException when a rule is broken; StatusPages turns it into a 422.
     */
    fun validateForListing(clock: Clock) {
        if (isExpired(clock)) {
            throw DomainRuleException(BEST_BEFORE_PASSED, field = "bestBeforeAt")
        }
        if (pickupWindow.until > bestBeforeAt) {
            throw DomainRuleException("The pickup window must not end after bestBeforeAt", field = "pickupWindow")
        }
        if (quantity < 1) {
            throw DomainRuleException("quantity must be at least 1", field = "quantity")
        }
        // Exactly on the limit is allowed; the message names the limit for this kind.
        if (shelfLifeRemaining(clock) > maxShelfLife) {
            throw DomainRuleException(
                "bestBeforeAt is too far away for $category: at most ${maxShelfLife.inWholeDays} days",
                field = "bestBeforeAt",
            )
        }
    }

    /**
     * Builds the subclass that belongs to [category] (§9.3).
     *
     * Takes plain values instead of a database row, so the model
     * does not depend on Exposed and the same factory serves both
     * the repository and a request to list a product.
     */
    companion object ProductFactory {
        /**
         * Builds the [FreshProduct], [FrozenProduct] or [AmbientProduct] that belongs to [category],
         * with the given values; [id] is 0 for a product that is not stored yet.
         */
        fun create(
            category: ProductCategory,
            id: Long,
            name: String,
            barcode: String?,
            originalPrice: Money,
            supplier: SupplierSummary,
            bestBeforeAt: Instant,
            quantity: Int,
            status: ProductStatus,
            pickupWindow: PickupWindow,
            allergens: Set<Allergen>,
        ): SurplusProduct = when (category) {
            ProductCategory.FRESH -> FreshProduct(
                id = id, supplier = supplier, name = name, barcode = barcode, originalPrice = originalPrice,
                bestBeforeAt = bestBeforeAt, quantity = quantity, status = status,
                pickupWindow = pickupWindow, allergens = allergens,
            )

            ProductCategory.FROZEN -> FrozenProduct(
                id = id, supplier = supplier, name = name, barcode = barcode, originalPrice = originalPrice,
                bestBeforeAt = bestBeforeAt, quantity = quantity, status = status,
                pickupWindow = pickupWindow, allergens = allergens,
            )

            ProductCategory.AMBIENT -> AmbientProduct(
                id = id, supplier = supplier, name = name, barcode = barcode, originalPrice = originalPrice,
                bestBeforeAt = bestBeforeAt, quantity = quantity, status = status,
                pickupWindow = pickupWindow, allergens = allergens,
            )
        }
    }
}
