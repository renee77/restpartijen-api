package com.restpartijen.api.plugins

import kotlinx.serialization.Serializable

/**
 * Machine-readable error codes (GI-4). One per exception in the table of §10.2.
 */
@Serializable
enum class ErrorCode {
    /** 400 – ValidationException, BadRequestException, RequestValidationException */
    VALIDATION_ERROR,

    /** 401 – UnauthorizedException */
    UNAUTHORIZED,

    /** 403 – ForbiddenException */
    FORBIDDEN,

    /** 404 – NotFoundException */
    NOT_FOUND,

    /** 409 – IllegalStateTransitionException, ConflictException */
    CONFLICT,

    /** 422 – DomainRuleException */
    DOMAIN_RULE_VIOLATION,

    /** 500 – any other exception */
    INTERNAL_ERROR,
}

/**
 * The one error shape for the whole API: { code, message, field? } (GI-4, decision 4.3).
 * Created only by StatusPages; features throw a DomainException instead.
 */
@Serializable
data class ErrorResponse(val code: ErrorCode, val message: String, val field: String? = null)
