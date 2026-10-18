package com.restpartijen.api.product.service

import com.restpartijen.api.product.model.SurplusProduct
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.shared.NotFoundException

class ProductService(private val repository: ProductRepository) {
    suspend fun getProduct(id: Long): SurplusProduct {
        return repository.findById(id) ?: throw NotFoundException("Product not found")
    }
}