package nl.restpartijen.app.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import nl.restpartijen.app.core.model.Role
import nl.restpartijen.app.core.model.Session
import nl.restpartijen.app.core.session.FakeSessionStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SessionAuthorizationTest {
    private val session = Session(token = "test-token", role = Role.COLLECTOR, supplierId = null)

    // The last request that reached the engine; the tests look at its headers.
    private var lastRequest: HttpRequestData? = null

    // Creates a client with the SessionAuthorization plugin installed and a given session store.
    private fun clientWith(store: FakeSessionStore) =
        HttpClient(
            MockEngine { request ->
                lastRequest = request
                respond(content = "", status = HttpStatusCode.OK)
            },
        ) {
            install(SessionAuthorization) { sessionStore = store }
        }

    @Test
    fun sendsTheTokenWhenThereIsASession() =
        runTest {
            // Assemble: Create a client with a session store that has a session.
            val client = clientWith(FakeSessionStore(session))

            // Act: Make a request.
            client.get("https://example.test/products")

            // Assert: The request has the Authorization header with the token.
            assertEquals("Bearer test-token", lastRequest?.headers?.get(HttpHeaders.Authorization))
        }

    @Test
    fun sendsNoHeaderWithoutASession() =
        runTest {
            val client = clientWith(FakeSessionStore())

            client.get("https://example.test/products")

            assertNull(lastRequest?.headers?.get(HttpHeaders.Authorization))
        }

    @Test
    fun stopsSendingTheTokenAfterTheSessionIsCleared() =
        runTest {
            val store = FakeSessionStore(session)
            val client = clientWith(store)

            client.get("https://example.test/products")
            assertEquals("Bearer test-token", lastRequest?.headers?.get(HttpHeaders.Authorization))

            store.clear()
            client.get("https://example.test/products")
            assertNull(lastRequest?.headers?.get(HttpHeaders.Authorization))
        }
}
