package com.restpartijen.api.product.routes

import com.restpartijen.api.product.dto.toResponse
import com.restpartijen.api.product.service.ProductService
import com.restpartijen.api.shared.ValidationException
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/**
 * Registers the F1 product routes.
 *
 * Walking skeleton version: only GET /api/v1/products/{id}.
 * The route knows the service, never the repository (§8.3).
 */
fun Application.configureProductRouting() {
    val service: ProductService by dependencies
    routing {
        get("/api/v1/products/{id}") {
            // Path parameters are always text; toLongOrNull avoids a 500 on input like "abc".
            val id = call.parameters["id"]?.toLongOrNull() ?: throw ValidationException("Product id must be a number")

            // Unknown id: the service throws NotFoundException; StatusPages turns it into a 404.
            val product = service.getProduct(id)

            // ContentNegotiation (configureSerialization) turns the @Serializable DTO into JSON.
            call.respond(product.toResponse())
        }
    }
}
