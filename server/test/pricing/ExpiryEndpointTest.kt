package com.restpartijen.api.pricing

import com.restpartijen.api.pricing.model.MaintenanceReport
import com.restpartijen.api.security.TestTokens
import com.restpartijen.api.shared.Role
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
/**
 * The test class for the 4 tests in ExpiryEndpointTest.kt. The tests are in a separate file so that they can be run with a different test app than the other pricing tests, which use the real ExpiryScheduler service. This test app uses a fake ExpiryScheduler that we can control.
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

        val token = TestTokens().createTestTokenWithRoleName(userId = 1L, roleName = "COLLECTOR", clock = Clock.System)

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

        val token = TestTokens().createTestTokenWithRoleName(userId = 1L, roleName = "ADMIN", clock = Clock.System)

        // Act: Make a post request to the protected route with the role that is not allowed to enter this route
        val response = client.post("/api/v1/admin/maintenance/expire") {
            bearerAuth(token)
        }

        // Assert See if the response matches and get the report
        assertEquals(HttpStatusCode.OK, response.status)
        println("BODY: ${response.bodyAsText()}")
        val report = Json.decodeFromString<MaintenanceReport>(response.bodyAsText())
        assertEquals(MaintenanceReport(lapsedReservations = 2, expiredProducts = 0), report)
    }

}