package com.restpartijen.api.product

import com.restpartijen.api.product.model.Product
import com.restpartijen.api.product.service.ProductService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


class ProductServiceTest {
    @Test
    fun `getProduct returns the product when the id exists`() {
        // Arrange
        val repository = FakeProductRepository(
            listOf(Product(id = 1, name = "Volkoren brood", category = "FRESH")),
        )
        val service = ProductService(repository)
        // Act
        val product = service.getProduct(1)
        // Assert
        assertEquals("Volkoren brood", product?.name)
    }

    @Test
    fun `getProduct returns null when the id does not exist`() {
        // Arrange
        val service = ProductService(FakeProductRepository(emptyList()))
        // Act
        val product = service.getProduct(999)
        // Assert
        assertNull(product)
    }
}


