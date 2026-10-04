package com.restpartijen.api.plugins

import com.restpartijen.api.shared.DomainRuleException
import com.restpartijen.api.shared.ForbiddenException
import com.restpartijen.api.shared.IllegalStateTransitionException
import com.restpartijen.api.shared.NotFoundException
import com.restpartijen.api.shared.UnauthorizedException
import com.restpartijen.api.shared.ValidationException
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * One integration test per status code from §10.2.
 * Each test route throws one exception; StatusPages must turn it into the right status and body.
 */
class StatusPagesTest {
    // Only the two plugins under test plus throwing routes: no module(), so no config or database needed.
    private fun Application.testApp() {
        configureSerialization()
        configureStatusPages()
        routing {
            get("/test/400") { throw ValidationException("Name is required", field = "name") }
            get("/test/401") { throw UnauthorizedException("Authentication required") }
            get("/test/403") { throw ForbiddenException("Not your product") }
            get("/test/404") { throw NotFoundException("Product 42 not found") }
            get("/test/409") { throw IllegalStateTransitionException("Cannot reserve a reserved product") }
            get("/test/422") { throw DomainRuleException("Best-before date is in the past") }
            get("/test/500") { throw IllegalStateException("db password is hunter2") }
        }
    }

    private fun ApplicationTestBuilder.setUp() = application { testApp() }

    private suspend fun HttpResponse.errorBody(): ErrorResponse =
        Json.decodeFromString<ErrorResponse>(bodyAsText())

    @Test
    fun `ValidationException gives 400 with the field`() = testApplication {
        // Arrange
        setUp()
        // Act
        val response = client.get("/test/400")
        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(ErrorResponse(ErrorCode.VALIDATION_ERROR, "Name is required", "name"), response.errorBody())
    }

    @Test
    fun `UnauthorizedException gives 401`() = testApplication {
        // Arrange
        setUp()
        // Act
        val response = client.get("/test/401")
        // Assert
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponse(ErrorCode.UNAUTHORIZED, "Authentication required"), response.errorBody())
    }

    @Test
    fun `ForbiddenException gives 403`() = testApplication {
        // Arrange
        setUp()
        // Act
        val response = client.get("/test/403")
        // Assert
        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertEquals(ErrorResponse(ErrorCode.FORBIDDEN, "Not your product"), response.errorBody())
    }

    @Test
    fun `NotFoundException gives 404`() = testApplication {
        // Arrange
        setUp()
        // Act
        val response = client.get("/test/404")
        // Assert
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(ErrorResponse(ErrorCode.NOT_FOUND, "Product 42 not found"), response.errorBody())
    }

    @Test
    fun `IllegalStateException gives 409`() = testApplication {
        // Arrange
        setUp()
        // Act
        val response = client.get("/test/409")
        // Assert
        assertEquals(HttpStatusCode.Conflict, response.status)
        assertEquals(ErrorResponse(ErrorCode.CONFLICT, "Cannot reserve a reserved product"), response.errorBody())
    }

    @Test
    fun `DomainRuleException gives 422`() = testApplication {
        // Arrange
        setUp()
        // Act
        val response = client.get("/test/422")
        // Assert
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(
            ErrorResponse(ErrorCode.DOMAIN_RULE_VIOLATION, "Best-before date is in the past"),
            response.errorBody(),
        )
    }

    @Test
    fun `unexpected exception gives 500 with a general message`() = testApplication {
        // Arrange
        setUp()
        // Act
        val response = client.get("/test/500")
        // Assert
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertEquals(ErrorResponse(ErrorCode.INTERNAL_ERROR, "An unexpected error occurred"), response.errorBody())
    }

    @Test
    fun `500 response leaks no exception message, class name or stacktrace`() = testApplication {
        // Arrange
        setUp()
        // Act
        val text = client.get("/test/500").bodyAsText()
        // Assert
        assertFalse("hunter2" in text, "exception message leaked")
        assertFalse("IllegalStateException" in text, "class name leaked")
        assertFalse("at com.restpartijen" in text, "stacktrace leaked")
    }
}