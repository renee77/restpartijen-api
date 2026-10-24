package com.restpartijen.api.reservation

import com.restpartijen.api.module
import com.restpartijen.api.testsupport.testConfig
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class ReservationModuleTest {

    @Test
    fun `module registers POST reservations`() = testApplication {
        // Arrange: the real application, as kotlin run starts it
        environment { config = testConfig() }
        application { module() }
        // Act
        val response = client.post("/api/v1/reservations")
        // Assert: 401 means the route exists behind requireRole; 404 would mean it is not registered
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `module registers GET reservations`() = testApplication {
        // Arrange
        environment { config = testConfig() }
        application { module() }
        // Act
        val response = client.get("/api/v1/reservations")
        // Assert
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `module registers GET products for search`() = testApplication {
        // Arrange: the real application, as kotlin run starts it
        environment { config = testConfig() }
        application { module() }
        // Act
        val response = client.get("/api/v1/products")
        // Assert: 200 without a token; 404 would mean the route is not registered
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
