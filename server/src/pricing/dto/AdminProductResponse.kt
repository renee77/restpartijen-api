package com.restpartijen.api.pricing.dto

import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.ProductView
import kotlinx.serialization.Serializable

/**
 * What the admin overview shows about one product (US-09).
 * No prices and no personal data (§15); the supplier is only named.
 *
 * @property id the product id
 * @property name the product name
 * @property status the current status, so the admin can see what can still be removed
 * @property supplierName the name of the supplier that offers the product
 * @property pickupWindowStart start of the pickup window, as an ISO-8601 instant
 * @property pickupWindowEnd end of the pickup window, as an ISO-8601 instant
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
