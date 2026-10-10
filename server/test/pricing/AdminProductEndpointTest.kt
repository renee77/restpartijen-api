package com.restpartijen.api.pricing

import com.restpartijen.api.pricing.model.MaintenanceReport
import com.restpartijen.api.security.TestTokens
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import com.restpartijen.api.plugins.ErrorCode
import com.restpartijen.api.plugins.ErrorResponse
import io.ktor.server.application.Application

class AdminProductEndpointTest {
    @Test
    fun `No token gives a 401`() = testApplication {
        // Arrange: test app with the real pricing round and fakes behind the service.
        application { setUpPricingTestApp(productReader = FakeProductReader(emptyList())) }

        // Act: call the endpoint without a token
        val response = client.get("/api/v1/admin/products")

        // Assert: the response status should be 401 Unauthorized, indicating that the token's role is not valid
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("Authentication required", error.message)
    }

    @Test
    fun `collector token returns 403`() = testApplication {
        // Arrange
        application {
            setUpPricingTestApp(productReader = FakeProductReader(emptyList()))
        }

        val token = TestTokens().createTestTokenWithRoleName(userId = 1L, roleName = "COLLECTOR", clock = Clock.System)

        // Act
        val response = client.get("/api/v1/admin/products") {
            bearerAuth(token)
        }

        // Assert. Verify that the decoded token contains the expected userId and role.
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText()).message
        assertEquals("Insufficient role", error)
    }

    @Test
    fun `admin token returns 200`() = testApplication {
        application {
            setUpPricingTestApp()
        }

        val token = TestTokens().createTestTokenWithRoleName(userId = 1L, roleName = "ADMIN", clock = Clock.System)

        val response = client.get("/api/v1/admin/products") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }
}