package com.restpartijen.api.product

import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import com.restpartijen.api.product.model.Product
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.product.service.ProductService
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest

class ProductServiceMockkTest {
    @Test
    fun `getProduct asks the repository, returns exactly one`() = runTest {
        // Arrange
        val mockRepository = mockk<ProductRepository>()
        coEvery { mockRepository.findById(1) } returns
            Product(1, "Volkoren Brood", "FRESH")
        val service = ProductService(mockRepository)

        // Act
        val product = service.getProduct(1)
        // Assert
        assertEquals("Volkoren Brood", product?.name)

        coVerify(exactly = 1) { mockRepository.findById(1) }
    }
}