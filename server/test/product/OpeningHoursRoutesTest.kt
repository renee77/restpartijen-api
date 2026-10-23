package com.restpartijen.api.product

import com.restpartijen.api.plugins.ErrorResponse
import com.restpartijen.api.product.dto.OpeningHoursResponse
import com.restpartijen.api.product.model.OpeningHours
import com.restpartijen.api.testsupport.FixedClock
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
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

    private val monday = OpeningHours(DayOfWeek.MONDAY, opensAt = LocalTime(8, 0), closesAt = LocalTime(21, 0))
    private val saturday = OpeningHours(DayOfWeek.SATURDAY, opensAt = LocalTime(9, 0), closesAt = LocalTime(18, 0))
}
