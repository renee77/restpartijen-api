package nl.restpartijen.app.core.network

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// The server sends one error shape for the whole API: { code, message, field? }
// (server/src/plugins/ErrorResponse.kt). These tests check that the app reads it.
class ErrorResponseDtoTest {
    @Test
    fun readsAnErrorWithAField() {
        val body = """{"code":"VALIDATION_ERROR","message":"Quantity must be positive","field":"quantity"}"""

        val error = Json.decodeFromString<ErrorResponseDto>(body)

        assertEquals(ErrorResponseDto("VALIDATION_ERROR", "Quantity must be positive", "quantity"), error)
    }

    @Test
    fun readsAnErrorWithoutAFieldAsNull() {
        // The server leaves out "field" when no single field caused the error.
        val body = """{"code":"NOT_FOUND","message":"Product not found"}"""

        val error = Json.decodeFromString<ErrorResponseDto>(body)

        assertNull(error.field)
    }
}
