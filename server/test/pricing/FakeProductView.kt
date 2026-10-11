package com.restpartijen.api.pricing

import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.ProductView
import com.restpartijen.api.shared.SupplierSummary
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class FakeProductView(override val id: Long) : ProductView {
    override val supplier = SupplierSummary(id = 1, name = "Fake supplier", latitude = 0.0, longitude = 0.0)
    override val name = "Fake product"
    override val status = ProductStatus.LISTED
    override val pickupWindow = PickupWindow(
        from = Instant.parse("2026-01-01T10:00:00Z"),
        until = Instant.parse("2026-01-01T12:00:00Z"),
    )
    override val originalPrice = Money(1000)
    override val category = ProductCategory.FRESH
    override fun shelfLifeRemaining(clock: Clock): Duration = Duration.ZERO
}