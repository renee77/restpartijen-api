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
) : SurplusProduct(id, supplier, name, barcode, originalPrice, bestBeforeAt, quantity,status, pickupWindow, allergens ){
    override val category: ProductCategory = ProductCategory.AMBIENT
    override val maxShelfLife: Duration = 1825.days // 5 years
}
