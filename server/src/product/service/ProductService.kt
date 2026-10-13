package com.restpartijen.api.product.service

import com.restpartijen.api.product.model.Product
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.shared.NotFoundException

class ProductService(private val repository: ProductRepository) {
    suspend fun getProduct(id: Long): Product {
        return repository.findById(id) ?: throw NotFoundException("Product not found")
    }
}