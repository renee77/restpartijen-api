package com.restpartijen.api.product

import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.testsupport.FixedClock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * ProductService.getProduct with a MockK mock as repository: checks the interaction itself,
 * that the service asks the repository exactly once.
 */
class ProductServiceMockkTest {
    @Test
    fun `getProduct asks the repository, returns exactly one`() = runTest {
        // Arrange
        val mockRepository = mockk<ProductRepository>()
        coEvery { mockRepository.findById(1) } returns
            aProduct(id = 1, name = "Volkoren Brood")
        val service =
            productService(mockRepository, fakeSupplierRepository(), FixedPriceProvider(), FixedClock(testNow))

        // Act
        val detail = service.getProduct(1)
        // Assert
        assertEquals("Volkoren Brood", detail.product.name)

        coVerify(exactly = 1) { mockRepository.findById(1) }
    }
}
