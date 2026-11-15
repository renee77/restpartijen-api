package nl.restpartijen.app.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
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

    // A client whose fake server always answers with [status].
    // [failOnError] mirrors `expectSuccess` of the real client (FTD app §8.6): when true,
    // Ktor throws an exception for a 4xx answer instead of returning it.
    private fun clientWith(
        store: FakeSessionStore,
        status: HttpStatusCode,
        failOnError: Boolean,
    ) = HttpClient(MockEngine { respond(content = "", status = status) }) {
        expectSuccess = failOnError
        install(SessionAuthorization) { sessionStore = store }
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

    // The request carried a token that is NOT the current session (a caller set its own header),
    // so a 401 for it must leave the current session alone (the rule from 1.2, 1.4 and 1.5).
    @Test
    fun `a 401 For Another Token Keeps The Session`() =
        runTest {
            val store = FakeSessionStore(session)
            val client = clientWith(store, HttpStatusCode.Unauthorized, failOnError = false)

            client.get(url) { header(HttpHeaders.Authorization, "Bearer other-token") }

            assertEquals(session, store.session.first())
        }
}
