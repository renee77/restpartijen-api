package com.restpartijen.api.product

import com.restpartijen.api.product.model.Product
import com.restpartijen.api.product.repository.ProductRepository

class FakeProductRepository(initialProducts: List<Product>) : ProductRepository {

    // One mutable copy, kept for the whole test, so create, update and delete change the same list.
    private val products = initialProducts.toMutableList()

    // Continue after the highest id already present, like an auto-increment column.
    private var nextId = (initialProducts.maxOfOrNull { it.id } ?: 0L) + 1

    override suspend fun findById(id: Long): Product? = products.find { it.id == id }

    override suspend fun findAll(): List<Product> = products.toList()

    override suspend fun create(item: Product): Product {
        val stored = item.copy(id = nextId++)
        products.add(stored)
        return stored
    }

    override suspend fun update(item: Product): Boolean {
        val index = products.indexOfFirst { it.id == item.id }
        if (index == -1) return false
        products[index] = item
        return true
    }

    override suspend fun delete(id: Long): Boolean = products.removeAll { it.id == id }
}
