package com.restpartijen.api.plugins

import com.restpartijen.api.shared.DomainRuleException
import com.restpartijen.api.shared.ForbiddenException
import com.restpartijen.api.shared.IllegalStateTransitionException
import com.restpartijen.api.shared.NotFoundException
import com.restpartijen.api.shared.UnauthorizedException
import com.restpartijen.api.shared.ValidationException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

/**
 * Turns every exception that escapes a route into one HTTP response with an ErrorResponse body
 * (GI-4, decision 4.1). One rule per exception, no `when` over all subclasses (B-22).
 */
fun Application.configureStatusPages() {
    install(StatusPages) {
        // 400: the only rule that fills `field`
        exception<ValidationException> { call, cause ->
            call.respondError(HttpStatusCode.BadRequest, ErrorCode.VALIDATION_ERROR, cause.message, cause.field)
        }
        // 401
        exception<UnauthorizedException> { call, cause ->
            call.respondError(HttpStatusCode.Unauthorized, ErrorCode.UNAUTHORIZED, cause.message)
        }
        // 403
        exception<ForbiddenException> { call, cause ->
            call.respondError(HttpStatusCode.Forbidden, ErrorCode.FORBIDDEN, cause.message)
        }
        // 404
        exception<NotFoundException> { call, cause ->
            call.respondError(HttpStatusCode.NotFound, ErrorCode.NOT_FOUND, cause.message)
        }
        // 409
        exception<IllegalStateTransitionException> { call, cause ->
            call.respondError(HttpStatusCode.Conflict, ErrorCode.CONFLICT, cause.message)
        }
        // 422
        exception<DomainRuleException> { call, cause ->
            call.respondError(HttpStatusCode.UnprocessableEntity, ErrorCode.DOMAIN_RULE_VIOLATION, cause.message)
        }
        // 500: anything unexpected. Stacktrace in the log, never in the response (decision 4.4)
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respondError(HttpStatusCode.InternalServerError, ErrorCode.INTERNAL_ERROR, "An unexpected error occurred")
        }
    }
}

/** Sends the one error shape. Falls back to a general text if the exception has no message. */
private suspend fun ApplicationCall.respondError(
    status: HttpStatusCode,
    code: ErrorCode,
    message: String?,
    field: String? = null,
) {
    respond(status, ErrorResponse(code, message ?: "An error occurred", field))
}