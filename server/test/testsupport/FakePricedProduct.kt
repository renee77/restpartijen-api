package com.restpartijen.api.pricing

import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PricedProduct
import com.restpartijen.api.shared.ProductCategory
import kotlin.time.Clock
import kotlin.time.Duration

/**
 * A PricedProduct with a fixed remaining shelf life, so a pricing test picks the tier directly.
 *
 * @property remaining what [shelfLifeRemaining] returns, whatever the clock says.
 */
class FakePricedProduct(
    override val originalPrice: Money,
    override val category: ProductCategory,
    private val remaining: Duration,
) : PricedProduct {

    override fun shelfLifeRemaining(clock: Clock): Duration = remaining
}
