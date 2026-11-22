package nl.restpartijen.app.core.network

import kotlinx.serialization.Serializable

/**
 * The API's error body: { code, message, field? } (server/src/plugins/ErrorResponse.kt).
 * Read from a 4xx/5xx response and then converted to [ApiError.Http], the model the screens use.
 *
 * @property code what went wrong, such as "NOT_FOUND". A String, not an enum, so a new
 *   code on the server does not break the app.
 * @property message the server's English text: log it, never show it to the user.
 * @property field the request field that caused the error, or null when no single field did.
 */
@Serializable
data class ErrorResponseDto(val code: String, val message: String, val field: String? = null)
