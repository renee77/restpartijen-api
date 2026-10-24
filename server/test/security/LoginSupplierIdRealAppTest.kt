package com.restpartijen.api.security

import com.restpartijen.api.module
import com.restpartijen.api.product.dto.ProductResponse
import com.restpartijen.api.security.dto.LoginRequest
import com.restpartijen.api.security.dto.LoginResponse
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.testConfig
import io.ktor.client.request.get
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The supplierId in the login answer on the real application (decision 3.8, A15, §18.4). */
class LoginSupplierIdRealAppTest {

    private val demoPassword = "Demo-Restpartij-2026"

    private fun ApplicationTestBuilder.startApp() {
        environment { config = testConfig() }
        application { module() }
    }

    private suspend fun ApplicationTestBuilder.login(email: String): LoginResponse {
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(email, demoPassword)))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        return Json.decodeFromString<LoginResponse>(response.bodyAsText())
    }

    @Test
    fun `a supplier gets a supplier id that the app can use`() = testApplication {
        startApp()

        val login = login("supermarkt@mail.com")

        assertEquals(Role.SUPPLIER, login.role)
        val supplierId = assertNotNull(login.supplierId)
        // The reason the app needs it: the products of its own supplier
        val products = client.get("/api/v1/suppliers/$supplierId/products")
        assertEquals(HttpStatusCode.OK, products.status)
        assertTrue(Json.decodeFromString<List<ProductResponse>>(products.bodyAsText()).isNotEmpty())
    }

    @Test
    fun `a collector and the admin get no supplier id`() = testApplication {
        startApp()

        assertNull(login("afhaler-a@mail.com").supplierId)
        assertNull(login("admin@mail.com").supplierId)
    }
}
