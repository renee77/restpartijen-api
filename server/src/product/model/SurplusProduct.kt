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

/**
 * A surplus product a supplier offers (§8.4).
 *
 * Sealed, so the compiler checks that the factory handles every kind. What is the same
 * for every kind lives here; each subclass only supplies its [category] and [maxShelfLife].
 * Not a data class: copy() would bypass the private setter on [status].
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

    // Final, because Kotlin does not allow a private setter on an open property.
    // Other features change the status only through ProductStatusUpdater.
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
     * @throws DomainRuleException when a rule is broken; StatusPages turns it into a 422.
     */
    fun validateForListing(clock: Clock) {
        if (isExpired(clock)) {
            throw DomainRuleException("bestBeforeAt must be in the future", field = "bestBeforeAt")
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
}
