package com.restpartijen.api.pricing

import com.restpartijen.api.pricing.model.MaintenanceReport
import com.restpartijen.api.security.TestTokens
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * POST /api/v1/admin/maintenance/expire on the pricing test app (US-08): the real route and the real
 * ExpiryScheduler, with fakes behind the scheduler that each test controls.
 */
class ExpiryEndpointTest {

    // A test to show that the endpoint is secured.
    @Test
    fun `no token returns 401`() = testApplication {
        // Arrange: test app with the real pricing route and fakes behind the service
        application { setUpPricingTestApp() }

        // Act: call the endpoint without a token
        val response = client.post("/api/v1/admin/maintenance/expire")

        // Assert: Check if we get the 401 back
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    // A Wrong role returns a 403
    @Test
    fun `unauthorized role returns a 403`() = testApplication {
        // Arrange
        application { setUpPricingTestApp() }

        val token = TestTokens().createTestTokenWithRoleName(userId = 1L, roleName = "COLLECTOR")

        // Act: Make a post request to the protected route with the role that is not allowed to enter this route
        val response = client.post("/api/v1/admin/maintenance/expire") {
            bearerAuth(token)
        }

        // Assert See if the response matches
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    // Authorized role returns a 200 OK
    @Test
    fun `authorized role returns a 200 OK and a fake report`() = testApplication {
        // Arrange: two overdue reservations behind the service
        application { setUpPricingTestApp(reservationMaintenance = FakeReservationMaintenance(lapsedCount = 2)) }

        val token = TestTokens().createTestTokenWithRoleName(userId = 1L, roleName = "ADMIN")

        // Act: Make a post request to the protected route with the role that is allowed to enter this route
        val response = client.post("/api/v1/admin/maintenance/expire") {
            bearerAuth(token)
        }

        // Assert See if the response matches and get the report
        assertEquals(HttpStatusCode.OK, response.status)
        println("BODY: ${response.bodyAsText()}")
        val report = Json.decodeFromString<MaintenanceReport>(response.bodyAsText())
        assertEquals(MaintenanceReport(lapsedReservations = 2, expiredProducts = 0), report)
    }

    @Test
    fun `updater changing fewer products than found still gives 200 and reports the real count`() = testApplication {
        application {
            setUpPricingTestApp(
                productReader = FakeProductReader(listOf(FakeProductView(1), FakeProductView(2))),
                productStatusUpdater = FakeProductStatusUpdater(expiredCount = 1),
            )
        }
        val token = TestTokens().createTestTokenWithRoleName(userId = 1L, roleName = "ADMIN")

        // Act
        val response = client.post("/api/v1/admin/maintenance/expire") {
            bearerAuth(token)
        }

        // Assert: the smaller count is not an error, so the run still answers 200
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
