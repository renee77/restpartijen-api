package com.restpartijen.api.product.dto

import com.restpartijen.api.product.model.SurplusProduct
import kotlinx.serialization.Serializable

/**
 * JSON response for a single product.
 *
 * Kept separate from the domain model so the API contract can change
 * without touching SurplusProduct, and vice versa.
 */
@Serializable
data class ProductResponse(
    val id: Long,
    val name: String,
    val category: String,
)

/** Maps the domain model to the API response. */
fun SurplusProduct.toResponse(): ProductResponse = ProductResponse(
    id = id,
    name = name,
    category = category,
)