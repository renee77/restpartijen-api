package com.restpartijen.api.product

import com.restpartijen.api.product.model.Product
import com.restpartijen.api.product.repository.ProductRepository

class FakeProductRepository(private val products: List<Product>) : ProductRepository {
    override fun findById(id: Long): Product? = products.find { it.id == id }
}

