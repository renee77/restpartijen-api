package com.restpartijen.api.product.service

import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import kotlin.time.Instant

/**
 * What a supplier may change of an own product (US-03, A13), in domain types.
 *
 * The same fields as [PlaceProductCommand] except category: the category decides the subclass, the
 * longest shelf life and the discount rules, so another category would be another product.
 * The route builds this from UpdateProductRequest; the service never sees the JSON format.
 * There is no pickup window: the service derives it again with the moment of updating (US-10).
 */
data class UpdateProductCommand(
    val name: String,
    val originalPrice: Money,
    val bestBeforeAt: Instant,
    val quantity: Int,
    val barcode: String? = null,
    val allergens: Set<Allergen> = emptySet(),
)
