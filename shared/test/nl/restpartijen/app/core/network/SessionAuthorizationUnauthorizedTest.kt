package nl.restpartijen.app.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import nl.restpartijen.app.core.model.Role
import nl.restpartijen.app.core.model.Session
import nl.restpartijen.app.core.session.FakeSessionStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SessionAuthorizationUnauthorizedTest {
    private val session = Session(token = "test-token", role = Role.COLLECTOR, supplierId = null)
    private val url = "https://api.example.test/products"

    // The last request that reached the fake server; some tests look at its headers.
    private var lastRequest: HttpRequestData? = null

    // A client whose fake server always answers with [status].
    // [failOnError] mirrors `expectSuccess` of the real client (FTD app §8.6): when true,
    // Ktor throws an exception for a 4xx answer instead of returning it.
    // [host] is the optional allowedHost setting of the plugin.
    private fun clientWith(
        store: FakeSessionStore,
        status: HttpStatusCode,
        failOnError: Boolean,
        host: String? = null,
    ) = HttpClient(
        MockEngine { request ->
            lastRequest = request
            respond(content = "", status = status)
        },
    ) {
        expectSuccess = failOnError
        install(SessionAuthorization) {
            sessionStore = store
            allowedHost = host
        }
    }

    // Does the plugin still see the 401 when `expectSuccess`
    // is on? This test fails (red) until step 3 is done. If the first assertion passes and only
    // the last one fails, you have learned that the exception is thrown as expected.
    @Test
    fun `a 401 With ExpectSuccess Throws And Clears The Session` () =
        runTest {
            val store = FakeSessionStore(session)
            val client = clientWith(store, HttpStatusCode.Unauthorized, failOnError = true)

            // The caller must still get the error, so the repository can turn it into ApiError.Http(401).
            val error = assertFailsWith<ClientRequestException> { client.get(url) }

            assertEquals(HttpStatusCode.Unauthorized, error.response.status)
            // The session is gone, because the request carried the token of the current session.
            assertNull(store.session.first())
        }

    @Test
    fun `a 401 Without ExpectSuccess Returns The Answer And Clears The Session` () =
        runTest {
            val store = FakeSessionStore(session)
            val client = clientWith(store, HttpStatusCode.Unauthorized, failOnError = false)

            val response = client.get(url)

            // Same behaviour when the setting is off: the 401 arrives as a normal answer.
            assertEquals(HttpStatusCode.Unauthorized, response.status)
            assertNull(store.session.first())
        }

    @Test
    fun `a 200 Keeps The Session` () =
        runTest {
            val store = FakeSessionStore(session)
            val client = clientWith(store, HttpStatusCode.OK, failOnError = true)

            client.get(url)

            assertEquals(session, store.session.first())
        }

    @Test
    fun `a 403 Keeps The Session` () =
        runTest {
            val store = FakeSessionStore(session)
            val client = clientWith(store, HttpStatusCode.Forbidden, failOnError = true)

            // 403 means "wrong kind of account" (FTD app §11.2), not "token expired".
            assertFailsWith<ClientRequestException> { client.get(url) }

            assertEquals(session, store.session.first())
        }

    @Test
    fun `a 401 Without A Session Does Nothing` () =
        runTest {
            // A visitor doing a protected action: no token, the server answers 401 (K-A3).
            val store = FakeSessionStore()
            val client = clientWith(store, HttpStatusCode.Unauthorized, failOnError = false)

            val response = client.get(url)

            // No crash, and the answer still reaches the caller.
            assertEquals(HttpStatusCode.Unauthorized, response.status)
            assertNull(store.session.first())
        }

    @Test
    fun `a Request After A401 Carries No Header` () =
        runTest {
            val store = FakeSessionStore(session)
            val client = clientWith(store, HttpStatusCode.Unauthorized, failOnError = false)

            client.get(url)
            assertEquals("Bearer test-token", lastRequest?.headers?.get(HttpHeaders.Authorization))

            // The session was cleared by the first answer, so the second request has no header (L4 + L5).
            client.get(url)
            assertNull(lastRequest?.headers?.get(HttpHeaders.Authorization))
        }

    @Test
    fun `a 401 For Another Token Keeps The Session` () =
        runTest {
            val store = FakeSessionStore(session)
            val client = clientWith(store, HttpStatusCode.Unauthorized, failOnError = false)

            // The caller sets its own token, which is not the token of the current session.
            // This stands for an old request answered after the user logged in again.
            client.get(url) { header(HttpHeaders.Authorization, "Bearer other-token") }

            assertEquals(session, store.session.first())
        }

    @Test
    fun `a 401 From Another Host Keeps The Session` () =
        runTest {
            val store = FakeSessionStore(session)
            val client =
                clientWith(store, HttpStatusCode.Unauthorized, failOnError = false, host = "api.example.test")

            // A third party must never be able to end our session.
            client.get("https://other.example.test/products")

            assertNull(lastRequest?.headers?.get(HttpHeaders.Authorization))
            assertEquals(session, store.session.first())
        }
}
