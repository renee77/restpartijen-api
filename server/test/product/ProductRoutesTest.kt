package com.restpartijen.api.product

import kotlin.test.Test
import io.ktor.server.testing.testApplication
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import com.restpartijen.api.module
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import com.restpartijen.api.product.dto.ProductResponse

class ProductRoutesTest {
    @Test
    fun `GET existing product returns 200 with the product as JSON`() = testApplication {
        // Arrange: in Ktor 3, testApplication does not load modules from application.yaml
        application { module() }
        // Act
        val response = client.get("/api/v1/products/1")
        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.decodeFromString<ProductResponse>(response.bodyAsText())
        assertEquals("Volkoren brood", body.name)
    }

    @Test
    fun `GET unknown product returns 404`() = testApplication {
        // Arrange
        application { module() }
        // Act
        val response = client.get("/api/v1/products/999")
        // Assert
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}