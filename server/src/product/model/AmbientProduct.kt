package com.restpartijen.api.product.model

import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.SupplierSummary
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * A shelf-stable product, kept at room temperature (§9.3).
 *
 * Adds only its kind and its longest plausible shelf life, five years (§9.10); everything else lives in
 * [SurplusProduct]. Production code builds it through [SurplusProduct.ProductFactory], so the category
 * always picks the right subclass.
 *
 * @param id the id the database gave the product; 0 for a product that is not stored yet
 * @param supplier name and coordinates of the supplier that offers the product
 * @param name the product name the supplier entered
 * @param barcode the barcode on the package, or null for a product without one
 * @param originalPrice the price the supplier asked, before any discount
 * @param bestBeforeAt the moment the best-before date ends; from then on the product has expired
 * @param quantity how many units are offered; at least 1 when listed
 * @param status the lifecycle status of the product, as stored or LISTED for a new one
 * @param pickupWindow when the product can be collected, derived from the supplier's opening hours (US-10)
 * @param allergens the EU allergens the product contains; empty when none were entered
 */
class AmbientProduct(
    id: Long = 0,
    supplier: SupplierSummary,
    name: String,
    barcode: String?,
    originalPrice: Money,
    bestBeforeAt: Instant,
    quantity: Int,
    status: ProductStatus,
    pickupWindow: PickupWindow,
    allergens: Set<Allergen>,
) : SurplusProduct(
    id,
    supplier,
    name,
    barcode,
    originalPrice,
    bestBeforeAt,
    quantity,
    status,
    pickupWindow,
    allergens,
) {
    override val category: ProductCategory = ProductCategory.AMBIENT
    override val maxShelfLife: Duration = 1825.days // 5 years
}
