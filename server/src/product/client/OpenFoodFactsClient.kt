package com.restpartijen.api.product.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import io.ktor.utils.io.readBuffer
import kotlinx.io.IOException
import kotlinx.io.readByteArray
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.nio.channels.UnresolvedAddressException

// Open Food Facts asks every app to name itself as AppName/Version (ContactEmail) (A5).
private const val APP_NAME_AND_VERSION = "restpartijen-api/0.1"

// The two fields we ask for are a few hundred bytes; a bigger body is no normal answer,
// and reading stops there (security F-02).
private const val MAX_BODY_BYTES = 64 * 1024

/**
 * Fetches the name and allergen tags for a barcode from the Open Food Facts API v3 (§10.3, A5).
 *
 * AutoCloseable, so the DI plugin closes the HttpClient when the application stops. The engine is not
 * closed here: HttpClient leaves an engine it was given open, and the DI closes that engine itself.
 *
 * @param engine CIO in production, a MockEngine in tests
 * @param settings the contact address for the User-Agent
 */
class OpenFoodFactsClient(engine: HttpClientEngine, settings: OpenFoodFactsSettings) : AutoCloseable {
    private val log = LoggerFactory.getLogger(OpenFoodFactsClient::class.java)

    // expectSuccess keeps Ktor's default (false): a 404 or 5xx does not throw,
    // so fetchProduct reads the status itself and picks the LookupResult.
    private val httpClient = HttpClient(engine) {
        install(HttpTimeout) {
            // The whole call, from connecting to the last byte; afterwards the lookup is Unavailable.
            requestTimeoutMillis = 3_000
        }
        install(UserAgent) {
            agent = "$APP_NAME_AND_VERSION (${settings.contactEmail})"
        }
        // The API answers directly; a redirect could send the call to another host (security F-02).
        followRedirects = false
    }

    // coerceInputValues: an explicit null for a field with a default takes the default, so
    // "allergens_tags": null still gives the name instead of a broken body.
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override fun close() = httpClient.close()

    /**
     * Asks Open Food Facts for the name and allergen tags of [barcode].
     * Never throws for a failure of Open Food Facts or the network; every such failure is logged and is Unavailable.
     *
     * @return Found with the name and the tags as sent, NotFound for a 404, or Unavailable
     */
    suspend fun fetchProduct(barcode: String): LookupResult = try {
        // prepareGet + execute streams the body: get() would first load all of it into memory,
        // so the 64 KiB limit below would come too late (code and security review, round 2).
        httpClient.prepareGet("https://world.openfoodfacts.org/api/v3/product") {
            // Encodes the barcode as one path segment, so it can never change the path or the query;
            // encodeSlash, because by default a "/" would split it into more segments.
            url { appendPathSegments(barcode, encodeSlash = true) }
            // Only the fields we use, so the answer stays small (§10.3).
            parameter("fields", "product_name,allergens_tags")
        }.execute { response -> response.toLookupResult() }
    } catch (e: IOException) {
        // No connection or no answer within the time-out; HttpRequestTimeoutException is an IOException.
        log.warn("Open Food Facts could not be reached: {}", e.javaClass.simpleName)
        LookupResult.Unavailable
    } catch (e: UnresolvedAddressException) {
        // No DNS, for example offline; CIO throws this IllegalArgumentException instead of an IOException.
        log.warn("The host of Open Food Facts could not be resolved")
        LookupResult.Unavailable
    } catch (e: SerializationException) {
        // A 200 with a body that is not valid JSON or has no product.
        log.warn("Open Food Facts sent a body that could not be read")
        LookupResult.Unavailable
    }

    // Picks the LookupResult from the status; only a 200 is read, and never more than MAX_BODY_BYTES of it.
    private suspend fun HttpResponse.toLookupResult(): LookupResult = when (status) {
        HttpStatusCode.OK -> {
            val text = textWithin(MAX_BODY_BYTES)
            if (text == null) {
                log.warn("Open Food Facts sent a body larger than {} bytes", MAX_BODY_BYTES)
                LookupResult.Unavailable
            } else {
                val body = json.decodeFromString<OpenFoodFactsResponse>(text)
                LookupResult.Found(body.product.productName, body.product.allergensTags)
            }
        }

        HttpStatusCode.NotFound -> LookupResult.NotFound

        // Rate limit (429), server errors (5xx), redirects and every other status: no usable answer.
        else -> {
            log.warn("Open Food Facts answered {}", status.value)
            LookupResult.Unavailable
        }
    }

    // Reads at most one byte more than allowed, so a bigger body is noticed without reading the rest;
    // leaving execute { } then closes the connection. The bytes are UTF-8, as JSON requires (RFC 8259).
    private suspend fun HttpResponse.textWithin(maxBytes: Int): String? {
        val bytes = bodyAsChannel().readBuffer(maxBytes + 1L).readByteArray()
        return if (bytes.size > maxBytes) null else bytes.decodeToString()
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
