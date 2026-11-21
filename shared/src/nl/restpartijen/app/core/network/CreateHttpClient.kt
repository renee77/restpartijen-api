package nl.restpartijen.app.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Creates the one HttpClient that every repository uses (FTD §8.6).
 *
 * The engine comes from the caller: OkHttp in the app, MockEngine in tests. So a test checks
 * exactly the settings the app uses.
 *
 * The client reads JSON into @Serializable classes, ignores fields it does not know, and throws
 * for a 4xx or 5xx response.
 */
fun createHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    install(ContentNegotiation) {
        // A new field on the server must not break the app.
        json(Json { ignoreUnknownKeys = true })
    }

    // Timeouts from the quality scenario "Geen netwerk" (FTD §8.7).
    install(HttpTimeout) {
        connectTimeoutMillis = 5_000
        requestTimeoutMillis = 15_000
    }

    // A 4xx or 5xx throws, so the conversion to ApiResult sees every error (A-22).
    expectSuccess = true
}
