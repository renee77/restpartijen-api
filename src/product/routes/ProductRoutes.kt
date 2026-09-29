package com.restpartijen.api.product.routes

import com.restpartijen.api.product.dto.toResponse
import com.restpartijen.api.product.service.ProductService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/**
 * Registers the F1 product routes.
 *
 * Walking skeleton version: only GET /api/v1/products/{id}.
 * The route knows the service, never the repository (§8.3).
 */
fun Application.configureProductRouting(service: ProductService) {
    routing {
        get("/api/v1/products/{id}") {
            // Path parameters are always text; toLongOrNull avoids a 500 on input like "abc".
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            // Unknown id: 404. Once StatusPages exists (T15), the service throws
            // NotFoundException and StatusPages builds this response instead.
            val product = service.getProduct(id)
            if (product == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            // ContentNegotiation (step 3c) turns the @Serializable DTO into JSON.
            call.respond(product.toResponse())
        }
    }
}
