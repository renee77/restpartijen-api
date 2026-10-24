package com.restpartijen.api.product.service

import com.restpartijen.api.shared.Allergen

/**
 * The suggestion the barcode lookup gives the supplier (§5.2); the supplier checks it before placing a product.
 *
 * @property name the product name, or null when Open Food Facts has no usable name; the supplier then types it.
 * @property allergens the EU allergens found; empty does not prove the product has none.
 * @property manualEntryRequired true only when Open Food Facts gave no answer, so the supplier fills in everything.
 */
data class ProductLookup(val name: String?, val allergens: Set<Allergen>, val manualEntryRequired: Boolean) {
    companion object {
        /** No usable answer from Open Food Facts, or a limit was reached: the supplier fills in everything. */
        val MANUAL_ENTRY = ProductLookup(name = null, allergens = emptySet(), manualEntryRequired = true)
    }
}
