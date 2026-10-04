package com.restpartijen.api.product

import com.restpartijen.api.persistence.Repository
import com.restpartijen.api.product.model.Product
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.testsupport.FakeRepository

/**
 * A ProductRepository for tests, backed by the generic FakeRepository (decision 6.4).
 *
 * Delegation (`by`) passes every Repository call on to the fake, so no repository logic is repeated here.
 * Once ProductRepository gets product-specific queries, this object has to implement those itself.
 */
fun fakeProductRepository(vararg products: Product): ProductRepository =
    object : ProductRepository, Repository<Product> by FakeRepository(
        idOf = { it.id },
        withId = { product, id -> product.copy(id = id) },
        initialItems = products.toList()
    ) {}
