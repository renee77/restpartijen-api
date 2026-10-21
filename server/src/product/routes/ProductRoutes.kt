package com.restpartijen.api.product.routes

import com.restpartijen.api.product.dto.PlaceProductRequest
import com.restpartijen.api.product.dto.toCommand
import com.restpartijen.api.product.dto.toResponse
import com.restpartijen.api.product.service.ProductService
import com.restpartijen.api.security.service.currentUser
import com.restpartijen.api.security.service.requireRole
import com.restpartijen.api.shared.Role
import com.restpartijen.api.shared.ValidationException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

/**
 * Registers the F1 product routes.
 *
 * GET /api/v1/products/{id} reads one product; POST /api/v1/products places one (US-01).
 * The route knows the service, never the repository (§8.3). It only handles HTTP: errors are
 * exceptions from the layers below, which StatusPages turns into the right status code (GI-4).
 */
fun Application.configureProductRouting() {
    val service: ProductService by dependencies
    routing {
        get("/api/v1/products/{id}") {
            // Path parameters are always text; toLongOrNull avoids a 500 on input like "abc".
            val id = call.parameters["id"]?.toLongOrNull() ?: throw ValidationException("Product id must be a number")

            // Unknown id: the service throws NotFoundException; StatusPages turns it into a 404.
            // The detail holds the product with its price breakdown (§5.7, SD-3).
            val detail = service.getProduct(id)

            // ContentNegotiation (configureSerialization) turns the @Serializable DTO into JSON.
            call.respond(detail.toResponse())
        }

        // Public, like GET /products/{id}: the offer of a supplier is visible to everyone (A4, US-03).
        get("/api/v1/suppliers/{id}/products") {
            val supplierId =
                call.parameters["id"]?.toLongOrNull() ?: throw ValidationException("Supplier id must be a number")

            // Unknown supplier: the service throws NotFoundException (404). No products gives an empty list.
            val productList = service.getSupplierProducts(supplierId)

            call.respond(productList.map { it.toResponse() })
        }
        // Only a supplier may place a product: no token gives 401, another role 403 (decision 3.6).
        requireRole(Role.SUPPLIER) {
            post("/api/v1/products") {
                // The JSON body becomes the request DTO; a missing field or unknown allergen gives a 400.
                // RequestValidation then checks its shape (validatePlaceProductRequest, 400 with the field).
                val request = call.receive<PlaceProductRequest>()

                // Turns the checked request into domain types.
                val command = request.toCommand()

                // The supplier comes from the user in the token, never from the body (§16.2).
                // The service checks the rules (403, 422) and stores the product.
                val detail = service.createProduct(call.currentUser().id, command)

                // 201: a new resource was made; the body is the stored product, with the id the database chose
                // and its price breakdown (§10.1).
                call.respond(HttpStatusCode.Created, detail.toResponse())
            }

            // Only a supplier may delete, and only an own LISTED product (US-03, §5.3).
            delete("/api/v1/products/{id}") {
                val productId =
                    call.parameters["id"]?.toLongOrNull() ?: throw ValidationException("Product id must be a number")

                // The user comes from the token; the service checks 404, 403 and 409 in that order.
                service.deleteProduct(call.currentUser().id, productId)

                // 204: done, and there is nothing to send back (§5.3).
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
