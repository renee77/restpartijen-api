package com.restpartijen.api.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidation

/**
 * Checks the content of request bodies before a route uses them (§10.2, ADR-05).
 *
 * Each feature registers its own validator here with one line, like in configureDependencies().
 * Convention (option B): a validator throws ValidationException(message, field) for invalid content,
 * so StatusPages answers 400 with the field filled in. A validator that returns
 * ValidationResult.Invalid still gives 400, via the safety-net rule in StatusPages, but without a field.
 */
fun Application.configureRequestValidation() {
    install(RequestValidation) {
        // F1 (Eva): POST /products and PUT /products/{id}
        // validate<CreateProductRequest>(::validateCreateProductRequest)
        // validate<UpdateProductRequest>(::validateUpdateProductRequest)

        // F2 (Stefan): POST /reservations
        // validate<CreateReservationRequest>(::validateCreateReservationRequest)

        // F3 (Lonneke): POST /auth/register (decision 3.7) and POST /auth/login
        // validate<RegisterRequest>(::validateRegisterRequest)
        // validate<LoginRequest>(::validateLoginRequest)
    }
}