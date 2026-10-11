package com.restpartijen.api.pricing

import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PricedProduct
import com.restpartijen.api.shared.ProductCategory
import kotlin.time.Clock
import kotlin.time.Duration

class FakePricedProduct(
    override val originalPrice: Money,
    override val category: ProductCategory,
    private val remaining: Duration
) : PricedProduct {

    override fun shelfLifeRemaining(clock: Clock): Duration {
        return remaining
    }
}