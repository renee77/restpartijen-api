package com.restpartijen.api.pricing.dto

import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.ProductView
import kotlinx.serialization.Serializable

/**
 * What the admin overview shows about one product (US-09).
 * No prices and no personal data (§15); the supplier is only named.
 */
@Serializable
data class AdminProductResponse(
    val id: Long,
    val name: String,
    val status: ProductStatus,
    val supplierName: String,
    val pickupWindowStart: String,
    val pickupWindowEnd: String,
)

/** Maps a product view to the admin response. */
fun ProductView.toAdminResponse() = AdminProductResponse(
    id = id,
    name = name,
    status = status,
    supplierName = supplier.name,
    pickupWindowStart = pickupWindow.from.toString(),
    pickupWindowEnd = pickupWindow.until.toString(),
)
