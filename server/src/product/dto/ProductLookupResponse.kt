package com.restpartijen.api.product.dto

import com.restpartijen.api.product.service.ProductLookup
import com.restpartijen.api.shared.Allergen
import kotlinx.serialization.Serializable

/**
 * JSON response of GET /api/v1/products/lookup/{barcode} (US-02, §5.2): a suggestion the supplier checks.
 *
 * Kept separate from ProductLookup, like ProductResponse, so the contract with the app can change on its own.
 * manualEntryRequired is true only when Open Food Facts gave no answer; the app then asks for everything.
 */
@Serializable
data class ProductLookupResponse(val name: String?, val allergens: Set<Allergen>, val manualEntryRequired: Boolean)

/** Maps the suggestion of the service to the API response; it only translates. */
fun ProductLookup.toResponse() = ProductLookupResponse(name, allergens, manualEntryRequired)
