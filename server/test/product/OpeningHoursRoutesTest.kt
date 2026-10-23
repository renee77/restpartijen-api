package com.restpartijen.api.product

import com.restpartijen.api.plugins.ErrorResponse
import com.restpartijen.api.product.dto.OpeningHoursResponse
import com.restpartijen.api.product.model.OpeningHours
import com.restpartijen.api.product.repository.SupplierRepository
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.FixedClock
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * The opening-hours routes of US-10 (§5.10), with the fake SupplierRepository behind the service.
 * The fake knows aSupplier (id 1), owned by supplierUserId.
 * Order of the checks for PUT: role (401/403), body (400), supplier exists (404), own supplier (403),
 * week rules (422); then 200 with the stored week.
 */
class OpeningHoursRoutesTest {

    // GET needs no token, but productRoutesTestApp also starts the security plugin.
    private val tokenClock = FixedClock(Clock.System.now())

    // --- GET /api/v1/suppliers/{id}/opening-hours ---

    @Test
    fun `GET opening hours returns 200 with the week as JSON, without a token`() = testApplication {
        // Arrange
        val suppliers = fakeSupplierRepository(aSupplier, ownerUserId = supplierUserId)
        suppliers.updateOpeningHours(aSupplier.id, listOf(monday, saturday))
        application { productRoutesTestApp(tokenClock, suppliers = suppliers) }

        // Act
        val response = client.get("/api/v1/suppliers/${aSupplier.id}/opening-hours")

        // Assert: days by name and times as "HH:mm", the format the app reads (§5.10)
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            listOf(
                OpeningHoursResponse(day = "MONDAY", opensAt = "08:00", closesAt = "21:00"),
                OpeningHoursResponse(day = "SATURDAY", opensAt = "09:00", closesAt = "18:00"),
            ),
            Json.decodeFromString<List<OpeningHoursResponse>>(response.bodyAsText()),
        )
    }

    @Test
    fun `GET opening hours of an unknown supplier returns 404`() = testApplication {
        // Arrange: the fake supplier repository knows only aSupplier
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = client.get("/api/v1/suppliers/999/opening-hours")

        // Assert: the service throws NotFoundException; StatusPages turns it into a 404
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET opening hours with a non-numeric id returns 400 and names the field`() = testApplication {
        // Arrange
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = client.get("/api/v1/suppliers/abc/opening-hours")

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("id", Json.decodeFromString<ErrorResponse>(response.bodyAsText()).field)
    }

    // --- PUT /api/v1/suppliers/{id}/opening-hours ---

    // Saturday first, so a sorted answer proves the stored week is returned instead of the request.
    private val validBody = """
        [{"day": "SATURDAY", "opensAt": "09:00", "closesAt": "18:00"},
         {"day": "MONDAY", "opensAt": "08:00", "closesAt": "21:00"}]
    """

    private fun tokenFor(role: Role, userId: Long = supplierUserId) = productToken(role, tokenClock, userId)

    private suspend fun ApplicationTestBuilder.replace(
        id: String = aSupplier.id.toString(),
        body: String = validBody,
        token: String? = tokenFor(Role.SUPPLIER),
    ): HttpResponse =
        client.put("/api/v1/suppliers/$id/opening-hours") {
            token?.let { bearerAuth(it) }
            contentType(ContentType.Application.Json)
            setBody(body)
        }

    // A fake that already holds a week, so a refused request can be checked to leave it alone.
    private suspend fun suppliersWithSaturday(): SupplierRepository =
        fakeSupplierRepository(aSupplier, ownerUserId = supplierUserId)
            .also { it.updateOpeningHours(aSupplier.id, listOf(saturday)) }

    @Test
    fun `PUT opening hours of the own supplier returns 200 with the stored week`() = testApplication {
        // Arrange: TC-10
        val suppliers = fakeSupplierRepository(aSupplier, ownerUserId = supplierUserId)
        application { productRoutesTestApp(tokenClock, suppliers = suppliers) }

        // Act
        val response = replace()

        // Assert: "MONDAY" and "08:00" were read into domain types, and stored
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            listOf(
                OpeningHoursResponse(day = "MONDAY", opensAt = "08:00", closesAt = "21:00"),
                OpeningHoursResponse(day = "SATURDAY", opensAt = "09:00", closesAt = "18:00"),
            ),
            Json.decodeFromString<List<OpeningHoursResponse>>(response.bodyAsText()),
        )
        assertEquals(listOf(monday, saturday), suppliers.getOpeningsHours(aSupplier.id))
    }

    @Test
    fun `PUT opening hours without a token returns 401`() = testApplication {
        // Arrange
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = replace(token = null)

        // Assert
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PUT opening hours as a collector returns 403`() = testApplication {
        // Arrange
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = replace(token = tokenFor(Role.COLLECTOR))

        // Assert: the role check in the route stops it, before the service
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `PUT opening hours of another supplier returns 403 and keeps its week`() = testApplication {
        // Arrange: user 8 is a supplier, but the fake gives this user no supplier, so not aSupplier
        val suppliers = suppliersWithSaturday()
        application { productRoutesTestApp(tokenClock, suppliers = suppliers) }

        // Act
        val response = replace(token = tokenFor(Role.SUPPLIER, userId = 8))

        // Assert
        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertEquals(listOf(saturday), suppliers.getOpeningsHours(aSupplier.id))
    }

    @Test
    fun `PUT opening hours of an unknown supplier returns 404`() = testApplication {
        // Arrange
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = replace(id = "999")

        // Assert
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `PUT opening hours with a non-numeric id returns 400 and names the field`() = testApplication {
        // Arrange
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = replace(id = "abc")

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("id", Json.decodeFromString<ErrorResponse>(response.bodyAsText()).field)
    }

    @Test
    fun `PUT opening hours with an unknown day returns 400`() = testApplication {
        // Arrange
        application { productRoutesTestApp(tokenClock) }

        // Act: Dutch, so not a value of DayOfWeek
        val response = replace(body = """[{"day": "MAANDAG", "opensAt": "08:00", "closesAt": "21:00"}]""")

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `PUT opening hours with a time that is not ISO 8601 returns 400`() = testApplication {
        // Arrange
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = replace(body = """[{"day": "MONDAY", "opensAt": "8 uur", "closesAt": "21:00"}]""")

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `PUT opening hours closing before opening returns 422 and keeps the old week`() = testApplication {
        // Arrange
        val suppliers = suppliersWithSaturday()
        application { productRoutesTestApp(tokenClock, suppliers = suppliers) }

        // Act
        val response = replace(body = """[{"day": "MONDAY", "opensAt": "21:00", "closesAt": "08:00"}]""")

        // Assert: the check in OpeningHours, reached through toOpeningHours in the route
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals("closesAt", Json.decodeFromString<ErrorResponse>(response.bodyAsText()).field)
        assertEquals(listOf(saturday), suppliers.getOpeningsHours(aSupplier.id))
    }

    @Test
    fun `PUT opening hours with an empty week returns 422 and keeps the old week`() = testApplication {
        // Arrange
        val suppliers = suppliersWithSaturday()
        application { productRoutesTestApp(tokenClock, suppliers = suppliers) }

        // Act
        val response = replace(body = "[]")

        // Assert
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals("openingHours", Json.decodeFromString<ErrorResponse>(response.bodyAsText()).field)
        assertEquals(listOf(saturday), suppliers.getOpeningsHours(aSupplier.id))
    }

    private val monday = OpeningHours(DayOfWeek.MONDAY, opensAt = LocalTime(8, 0), closesAt = LocalTime(21, 0))
    private val saturday = OpeningHours(DayOfWeek.SATURDAY, opensAt = LocalTime(9, 0), closesAt = LocalTime(18, 0))
}
