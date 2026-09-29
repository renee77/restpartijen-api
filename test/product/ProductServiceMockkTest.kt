package com.restpartijen.api.product

import io.mockk.mockk
import io.mockk.every
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import com.restpartijen.api.product.model.Product
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.product.service.ProductService

class ProductServiceMockkTest {
    @Test
    fun `getProduct asks the repository, returns exactly one` () {
        val mockRepository = mockk<ProductRepository>()
        every { mockRepository.findById(1) } returns
                Product(1, "Volkoren Brood", "FRESH")
        val services = ProductService(mockRepository)

        // Act
        val product = services.getProduct(1)
        // Assert
        assertEquals("Volkoren Brood", product?.name)

        verify(exactly = 1) { mockRepository.findById(1) }
    }

}