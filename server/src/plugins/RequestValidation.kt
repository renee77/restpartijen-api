package com.restpartijen.api.plugins

import com.restpartijen.api.product.dto.PlaceProductRequest
import com.restpartijen.api.product.dto.UpdateProductRequest
import com.restpartijen.api.product.dto.validatePlaceProductRequest
import com.restpartijen.api.product.dto.validateUpdateProductRequest
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
        validate<PlaceProductRequest>(::validatePlaceProductRequest)
        validate<UpdateProductRequest>(::validateUpdateProductRequest)

        // POST /auth/register is not checked here: RegistrationService calls RegistrationValidator (A11).
    }
}
