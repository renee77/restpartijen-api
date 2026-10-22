package com.restpartijen.api.product.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import kotlinx.io.IOException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.nio.channels.UnresolvedAddressException

/**
 * Fetches the name and allergen tags for a barcode from the Open Food Facts API v3 (§10.3, A5).
 *
 * AutoCloseable, so the DI plugin closes the HttpClient when the application stops. The engine is not
 * closed here: HttpClient leaves an engine it was given open, and the DI closes that engine itself.
 *
 * @param engine CIO in production, a MockEngine in tests
 */
class OpenFoodFactsClient(engine: HttpClientEngine) : AutoCloseable {
    // expectSuccess keeps Ktor's default (false): a 404 or 5xx does not throw,
    // so fetchProduct reads the status itself and picks the LookupResult.
    private val httpClient = HttpClient(engine) {
        install(HttpTimeout) {
            // The whole call, from connecting to the last byte; afterwards the lookup is Unavailable.
            requestTimeoutMillis = 3_000
        }
        install(UserAgent) {
            // Open Food Facts asks every app to name itself as AppName/Version (ContactEmail) (A5).
            agent = "restpartijen-api/0.1 (etm.bouwman@student.avans.nl)"
        }
    }
    private val json = Json { ignoreUnknownKeys = true }

    override fun close() = httpClient.close()

    suspend fun fetchProduct(barcode: String): LookupResult {
        return try {
            val response = httpClient.get("https://world.openfoodfacts.org/api/v3/product") {
                // Encodes the barcode as one path segment, so it can never change the path or the query;
                // encodeSlash, because by default a "/" would split it into more segments.
                url { appendPathSegments(barcode, encodeSlash = true) }
                // Only the fields we use, so the answer stays small (§10.3).
                parameter("fields", "product_name,allergens_tags")
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val body = json.decodeFromString<OpenFoodFactsResponse>(response.bodyAsText())
                    LookupResult.Found(body.product.productName, body.product.allergensTags)
                }

                HttpStatusCode.NotFound -> LookupResult.NotFound
                // Rate limit (429), server errors (5xx) and every other status: Open Food Facts gave no usable answer.
                else -> LookupResult.Unavailable
            }
        } catch (e: IOException) {
            // No connection or no answer within the time-out; HttpRequestTimeoutException is an IOException.
            LookupResult.Unavailable
        } catch (e: UnresolvedAddressException) {
            // No DNS, for example offline; CIO throws this IllegalArgumentException instead of an IOException.
            LookupResult.Unavailable
        } catch (e: SerializationException) {
            // A 200 with a body that is not valid JSON or has no product.
            LookupResult.Unavailable
        }
    }
}

// Only the outer envelope of the Open Food Facts answer; every other field is skipped.
@Serializable
private data class OpenFoodFactsResponse(
    // No default: a 200 without a product is a broken body, so decoding fails and the lookup is Unavailable.
    val product: OpenFoodFactsProduct,
)

// Open Food Facts is filled by its community, so either field can be missing.
@Serializable
private data class OpenFoodFactsProduct(
    @SerialName("product_name") val productName: String? = null,
    @SerialName("allergens_tags") val allergensTags: List<String> = emptyList(),
)
