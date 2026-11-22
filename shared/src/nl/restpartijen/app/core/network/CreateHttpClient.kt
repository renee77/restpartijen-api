package nl.restpartijen.app.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import nl.restpartijen.app.core.session.SessionStore

/**
 * Creates the one HttpClient that every repository uses (FTD §8.6).
 *
 * The engine and the session store come from the caller: OkHttp and the real store in the app,
 * MockEngine and a fake store in tests. So a test checks exactly the settings the app uses.
 * The caller owns the engine: closing the client does not close it.
 *
 * The client sends a relative path such as `get("products")` to [ApiConfig.BASE_URL], adds the token of
 * the current session for the API host only, reads JSON into @Serializable classes, ignores fields it
 * does not know, does not follow redirects, and throws for a 3xx, 4xx or 5xx response.
 */
fun createHttpClient(engine: HttpClientEngine, sessionStore: SessionStore): HttpClient = HttpClient(engine) {
    // The base URL lives here, so no repository knows the address of the API.
    defaultRequest {
        url(ApiConfig.BASE_URL)
    }

    install(ContentNegotiation) {
        // A new field on the server must not break the app.
        json(Json { ignoreUnknownKeys = true })
    }

    // Timeouts from the quality scenario "Geen netwerk" (FTD §8.7).
    install(HttpTimeout) {
        connectTimeoutMillis = 5_000
        requestTimeoutMillis = 15_000
    }

    // The SessionAuthorization plugin adds the token (A-30). allowedHost keeps the token away from any other
    // host; without it, every host would get it.
    install(SessionAuthorization) {
        this.sessionStore = sessionStore
        allowedHost = ApiConfig.HOST
    }

    // The API never redirects, so a redirect means a wrong address or something between app and server.
    // Throwing makes that visible instead of silently following it. (Ktor already drops the token when a
    // redirect goes to another host or port, so this is not about leaking the token.)
    followRedirects = false

    // A 3xx, 4xx or 5xx throws, so the conversion to ApiResult sees every error (A-22).
    expectSuccess = true
}
