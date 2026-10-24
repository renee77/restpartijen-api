package com.restpartijen.api.pricing

import com.restpartijen.api.pricing.model.AmbientDiscountPolicy
import com.restpartijen.api.pricing.model.DiscountPolicyResolver
import com.restpartijen.api.pricing.model.FreshDiscountPolicy
import com.restpartijen.api.pricing.model.FrozenDiscountPolicy
import com.restpartijen.api.shared.ProductCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/** DiscountPolicyResolver: every category gets its own policy, with the right maximum discount. */
class DiscountPolicyResolverTest {
    @Test
    fun `fresh policy applies to fresh category`() {
        val policy = FreshDiscountPolicy()

        assertEquals(ProductCategory.FRESH, policy.appliesTo())
    }

    @Test
    fun `frozen policy applies to frozen category`() {
        val policy = FrozenDiscountPolicy()

        assertEquals(ProductCategory.FROZEN, policy.appliesTo())
    }

    @Test
    fun `ambient policy applies to ambient category`() {
        val policy = AmbientDiscountPolicy()

        assertEquals(ProductCategory.AMBIENT, policy.appliesTo())
    }

    @Test
    fun `fresh policy has maximum discount of 60 percent`() {
        assertEquals(60, FreshDiscountPolicy().maxDiscount())
    }

    @Test
    fun `frozen policy has maximum discount of 40 percent`() {
        assertEquals(40, FrozenDiscountPolicy().maxDiscount())
    }

    @Test
    fun `ambient policy has maximum discount of 40 percent`() {
        assertEquals(40, AmbientDiscountPolicy().maxDiscount())
    }

    @Test
    fun `resolver returns a policy for every product category`() {
        ProductCategory.entries.forEach { category ->
            val policy = DiscountPolicyResolver.resolve(category)

            assertNotNull(policy)
        }
    }

    @Test
    fun `resolved policy matches the requested category`() {
        ProductCategory.entries.forEach { category ->
            val policy = DiscountPolicyResolver.resolve(category)

            assertEquals(category, policy.appliesTo())
        }
    }
}
