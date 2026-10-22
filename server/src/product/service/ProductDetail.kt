package com.restpartijen.api.product.service

import com.restpartijen.api.product.model.SurplusProduct
import com.restpartijen.api.shared.PriceBreakdown
import com.restpartijen.api.shared.ProductStatus

/**
 * What ProductService returns for one product: the product with its price breakdown (§5.7, SD-3).
 *
 * Holds domain types only, so the service does not know the JSON format; the route turns this into
 * a ProductResponse. The price comes from PricingService via PriceProvider (K-4), F1 does not calculate it.
 *
 * @property price null when the product has expired (B-18)
 * @property status the status to show; an expired product that is still LISTED shows as EXPIRED (SD-3),
 *   because the status updater (US-08) may not have run yet
 */
data class ProductDetail(
    val product: SurplusProduct,
    val price: PriceBreakdown?,
    val status: ProductStatus,
)
