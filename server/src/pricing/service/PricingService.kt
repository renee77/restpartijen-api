package com.restpartijen.api.pricing.service

import com.restpartijen.api.pricing.model.DiscountPolicyResolver
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PriceBreakdown
import com.restpartijen.api.shared.PriceProvider
import com.restpartijen.api.shared.PricedProduct
import kotlin.time.Clock

class PricingService(private val clock: Clock) : PriceProvider {

    override fun currentPrice(product: PricedProduct): Money? {
        return priceBreakdown(product)?.currentPrice
    }

    override fun priceBreakdown(product: PricedProduct): PriceBreakdown? {
        // Get the remaining shelf life using the service clock.
        val remaining = product.shelfLifeRemaining(clock)

        // Return null if the product is expired.
        if (remaining <= kotlin.time.Duration.ZERO) {
            return null
        }

        // Resolve the discount policy for the product category.
        val policy = DiscountPolicyResolver.resolve(product.category)

        // Determine the discount percentage for the remaining shelf life.
        val percentage = policy.discountPercentage(remaining)

        // Calculate the current price.
        val currentPriceCents = product.originalPrice.cents * (100 - percentage).toLong() / 100

        // Build the price breakdown with the original price, discount, and current price.
        return PriceBreakdown(
            originalPrice = product.originalPrice,
            discountPercentage = percentage,
            currentPrice = Money.fromCents(currentPriceCents)
        )
    }
}