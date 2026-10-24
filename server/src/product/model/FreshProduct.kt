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
 * A chilled fresh product, such as dairy, meat or bread (§9.3).
 *
 * Adds only its kind and its longest plausible shelf life, 14 days (§9.10); everything else lives in
 * [SurplusProduct], which also explains the constructor parameters. Production code builds it through
 * [SurplusProduct.ProductFactory], so the category always picks the right subclass.
 */
class FreshProduct(
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
    override val category: ProductCategory = ProductCategory.FRESH
    override val maxShelfLife: Duration = 14.days
}
