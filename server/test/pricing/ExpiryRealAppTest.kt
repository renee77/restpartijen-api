package com.restpartijen.api.pricing

import com.restpartijen.api.module
import com.restpartijen.api.pricing.model.MaintenanceReport
import com.restpartijen.api.security.dto.LoginRequest
import com.restpartijen.api.security.dto.LoginResponse
import com.restpartijen.api.testsupport.testConfig
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The expire endpoint on the real application, as kotlin run starts it: real module(), seed data
 * and a real token from /auth/login (GI-6, US-08).
 *
 * module() always uses the same in-memory database, so these tests share it. That is why the
 * exact numbers of one run are not checked here; the integration test with its own database does that.
 */
class ExpiryRealAppTest {

    // The one demo password of all seed accounts (SeedData, README.md)
    private val demoPassword = "Demo-Restpartij-2026"

    private fun ApplicationTestBuilder.startApp() {
        environment { config = testConfig() }
        application { module() }
    }

    /** Logs in as a seed account and returns the token. */
    private suspend fun ApplicationTestBuilder.loginAs(email: String): String {
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(email, demoPassword)))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        return Json.decodeFromString<LoginResponse>(response.bodyAsText()).token
    }

    @Test
    fun `no token gives 401`() = testApplication {
        startApp()

        val response = client.post("/api/v1/admin/maintenance/expire")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `a collector gets 403`() = testApplication {
        startApp()
        val token = loginAs("afhaler-a@mail.com")

        val response = client.post("/api/v1/admin/maintenance/expire") { bearerAuth(token) }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `a supplier gets 403`() = testApplication {
        startApp()
        val token = loginAs("supermarkt@mail.com")

        val response = client.post("/api/v1/admin/maintenance/expire") { bearerAuth(token) }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `the admin gets 200, and a second run reports zero`() = testApplication {
        startApp()
        val token = loginAs("admin@mail.com")

        // First run: 200 with a report. The numbers depend on the shared database, so only the shape is checked.
        val first = client.post("/api/v1/admin/maintenance/expire") { bearerAuth(token) }
        assertEquals(HttpStatusCode.OK, first.status)
        Json.decodeFromString<MaintenanceReport>(first.bodyAsText())

        // Second run right after: nothing is left to do, whatever the first run did (idempotent, §5.8)
        val second = client.post("/api/v1/admin/maintenance/expire") { bearerAuth(token) }
        assertEquals(HttpStatusCode.OK, second.status)
        assertEquals(
            MaintenanceReport(lapsedReservations = 0, expiredProducts = 0),
            Json.decodeFromString<MaintenanceReport>(second.bodyAsText()),
        )
    }
}
