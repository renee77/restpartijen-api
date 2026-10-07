package com.restpartijen.api.plugins

import kotlinx.serialization.Serializable

/**
 * Machine-readable error codes (GI-4). One per exception in the table of §10.2.
 */
@Serializable
enum class ErrorCode {
    VALIDATION_ERROR,       // 400 – ValidationException
    UNAUTHORIZED,           // 401 – UnauthorizedException
    FORBIDDEN,              // 403 – ForbiddenException
    NOT_FOUND,              // 404 – NotFoundException
    CONFLICT,               // 409 – IllegalStateTransitionException
    DOMAIN_RULE_VIOLATION,  // 422 – DomainRuleException
    INTERNAL_ERROR          // 500 – any other exception
}

/**
 * The one error shape for the whole API: { code, message, field? } (GI-4, decision 4.3).
 * Created only by StatusPages; features throw a DomainException instead.
 */
@Serializable
data class ErrorResponse(
    val code: ErrorCode,
    val message: String,
    val field: String? = null,
)