package com.restpartijen.api.pricing

import com.restpartijen.api.module
import com.restpartijen.api.pricing.dto.AdminProductResponse
import com.restpartijen.api.security.dto.LoginRequest
import com.restpartijen.api.security.dto.LoginResponse
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.testsupport.testConfig
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The admin endpoints on the real application: real module(), seed data and a real token.
 * module() always uses the same in-memory database, so nothing here may change data.
 */
class AdminProductsRealAppTest {

    private val demoPassword = "Demo-Restpartij-2026"

    private fun ApplicationTestBuilder.startApp() {
        environment { config = testConfig() }
        application { module() }
    }

    private suspend fun ApplicationTestBuilder.loginAs(email: String): String {
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(email, demoPassword)))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        return Json.decodeFromString<LoginResponse>(response.bodyAsText()).token
    }

    @Test
    fun `the overview without a token gives 401`() = testApplication {
        startApp()

        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/admin/products").status)
    }

    @Test
    fun `the overview as collector or supplier gives 403`() = testApplication {
        startApp()

        for (email in listOf("afhaler-a@mail.com", "supermarkt@mail.com")) {
            val token = loginAs(email)
            val response = client.get("/api/v1/admin/products") { bearerAuth(token) }
            assertEquals(HttpStatusCode.Forbidden, response.status, "role of $email")
        }
    }

    @Test
    fun `the admin overview hides removed products`() = testApplication {
        startApp()
        val token = loginAs("admin@mail.com")

        val response = client.get("/api/v1/admin/products") { bearerAuth(token) }

        assertEquals(HttpStatusCode.OK, response.status)
        val products = Json.decodeFromString<List<AdminProductResponse>>(response.bodyAsText())
        assertTrue(products.isNotEmpty())
        assertTrue(products.none { it.status == ProductStatus.REMOVED })
    }

    @Test
    fun `the admin overview with REMOVED shows the removed products`() = testApplication {
        startApp()
        val token = loginAs("admin@mail.com")

        val response = client.get("/api/v1/admin/products?status=REMOVED") { bearerAuth(token) }

        assertEquals(HttpStatusCode.OK, response.status)
        val products = Json.decodeFromString<List<AdminProductResponse>>(response.bodyAsText())
        assertTrue(products.all { it.status == ProductStatus.REMOVED })
        assertTrue(products.any { it.name == "Appelsap 1,5 liter" })
    }

    @Test
    fun `an unknown status gives 400 and names the field`() = testApplication {
        startApp()
        val token = loginAs("admin@mail.com")

        val response = client.get("/api/v1/admin/products?status=BESTAATNIET") { bearerAuth(token) }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("\"field\":\"status\""))
    }

    @Test
    fun `delete without a token gives 401, and as collector 403`() = testApplication {
        startApp()

        // Both are refused before the service runs, so nothing is deleted
        assertEquals(HttpStatusCode.Unauthorized, client.delete("/api/v1/admin/products/1").status)

        val token = loginAs("afhaler-a@mail.com")
        val response = client.delete("/api/v1/admin/products/1") { bearerAuth(token) }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }
}