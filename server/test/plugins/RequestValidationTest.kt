package com.restpartijen.api.plugins

import com.restpartijen.api.shared.ValidationException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

// Test-only request body: one required String and one enum, like a real POST /products.
@Serializable
enum class TestCategory { FRESH, FROZEN }

@Serializable
data class TestProductRequest(val name: String, val category: TestCategory)

/**
 * S4: every kind of invalid body ends as 400 in the error model, never 500 (§10.2).
 */
class RequestValidationTest {

    // The plugins under test, a validator on the test DTO, and one POST route. No module(), so no config.
    private fun Application.testApp() {
        configureSerialization()
        configureStatusPages()
        install(RequestValidation) {
            validate<TestProductRequest> { request ->
                // Throw our own exception, so StatusPages fills `field`
                if (request.name.isBlank()) throw ValidationException("Name is required", field = "name")
                // The Ktor way, to test the safety-net rule
                if (request.name.length > 50) ValidationResult.Invalid("Name is too long")
                else ValidationResult.Valid
            }
        }
        routing {
            post("/test/products") {
                val request = call.receive<TestProductRequest>()
                call.respond(HttpStatusCode.Created, request)
            }
        }
    }

    private fun ApplicationTestBuilder.setUp() = application { testApp() }

    private suspend fun ApplicationTestBuilder.postJson(json: String): HttpResponse =
        client.post("/test/products") {
            contentType(ContentType.Application.Json)
            setBody(json)
        }

    private suspend fun HttpResponse.errorBody(): ErrorResponse =
        Json.decodeFromString<ErrorResponse>(bodyAsText())

    @Test
    fun `valid body passes and the route answers 201`() = testApplication {
        // Arrange
        setUp()
        // Act
        val response = postJson("""{"name": "Volkoren brood", "category": "FRESH"}""")
        // Assert: control test, proves the 400s below come from validation and not from the route
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `missing required field gives 400 with that field`() = testApplication {
        // Arrange
        setUp()
        // Act
        val response = postJson("""{"category": "FRESH"}""")
        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(
            ErrorResponse(ErrorCode.VALIDATION_ERROR, "Request is invalid or incomplete", "name"),
            response.errorBody(),
        )
    }

    @Test
    fun `unknown enum value gives 400`() = testApplication {
        // Arrange
        setUp()
        // Act
        val response = postJson("""{"name": "Volkoren brood", "category": "DEODERANT"}""")
        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(ErrorCode.VALIDATION_ERROR, response.errorBody().code)
    }

    @Test
    fun `read error does not leak internal class names`() = testApplication {
        // Arrange
        setUp()
        // Act
        val text = postJson("""{"name": "Volkoren brood", "category": "VEGGIE"}""").bodyAsText()
        // Assert
        assertFalse("com.restpartijen" in text, "internal class name leaked")
        assertFalse("TestProductRequest" in text, "DTO name leaked")
    }

    @Test
    fun `blank name gives 400 with the field via ValidationException`() = testApplication {
        // Arrange
        setUp()
        // Act
        val response = postJson("""{"name": "  ", "category": "FRESH"}""")
        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(ErrorResponse(ErrorCode.VALIDATION_ERROR, "Name is required", "name"), response.errorBody())
    }

    // TODO (yours): validator returning Invalid gives 400 via the safety net
    @Test
    fun `validator returning Invalid gives 400`() = testApplication {
        // Arrange
        setUp()
        // Act
        val longName = "x".repeat(51)
        val response = postJson("""{"name": "$longName", "category": "FRESH"}""")
        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(ErrorResponse(ErrorCode.VALIDATION_ERROR, "Name is too long"), response.errorBody())

    }
}