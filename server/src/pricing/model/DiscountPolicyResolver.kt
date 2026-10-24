package com.restpartijen.api.pricing.model

import com.restpartijen.api.shared.ProductCategory

/**
 * Resolves the discount policy for a product category.
 *
 * Each supported product category is mapped to its corresponding discount policy.
 * A missing policy is treated as a programming error because every supported
 * category must have a discount policy.
 */
object DiscountPolicyResolver {

    // Initializes the map only when it is accessed for the first time.
    // The initialized map is then reused for subsequent accesses.
    private val policies: Map<ProductCategory, DiscountPolicy> by lazy {
        mapOf(
            ProductCategory.FRESH to FreshDiscountPolicy(),
            ProductCategory.FROZEN to FrozenDiscountPolicy(),
            ProductCategory.AMBIENT to AmbientDiscountPolicy(),
        )
    }

    /**
     * Returns the discount policy for the given product category.
     *
     * @param category the product category for which to resolve the policy
     * @return the discount policy associated with the category
     * @throws IllegalStateException if no policy is registered for the category
     */
    fun resolve(category: ProductCategory): DiscountPolicy =
        policies[category] ?: error("Geen korting geregistreerd voor de categorie: $category")
}
