package nl.restpartijen.app.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.client.request.header
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
    private fun clientWith(store: FakeSessionStore, host: String? = null) = HttpClient(
        MockEngine { request ->
            lastRequest = request
            respond(content = "", status = HttpStatusCode.OK)
        },
    ) {
        install(SessionAuthorization) {
            sessionStore = store
            allowedHost = host
        }
    }

    private fun authorization() = lastRequest?.headers?.get(HttpHeaders.Authorization)

    @Test
    fun sendsTheTokenWhenThereIsASession() = runTest {
        clientWith(FakeSessionStore(session)).get("https://api.example.test/products")

        assertEquals("Bearer test-token", authorization())
    }

    @Test
    fun sendsNoHeaderWithoutASession() = runTest {
        clientWith(FakeSessionStore()).get("https://api.example.test/products")

        assertNull(authorization())
    }

    @Test
    fun stopsSendingTheTokenAfterTheSessionIsCleared() = runTest {
        val store = FakeSessionStore(session)
        val client = clientWith(store)

        client.get("https://api.example.test/products")
        assertEquals("Bearer test-token", authorization())

        store.clear()
        client.get("https://api.example.test/products")
        assertNull(authorization())
    }

    @Test
    fun keepsAnAuthorizationHeaderThatIsAlreadySet() = runTest {
        clientWith(FakeSessionStore(session)).get("https://api.example.test/products") {
            header(HttpHeaders.Authorization, "Bearer other-token")
        }

        assertEquals("Bearer other-token", authorization())
    }

    @Test
    fun sendsTheTokenToTheAllowedHost() = runTest {
        clientWith(FakeSessionStore(session), host = "api.example.test").get("https://api.example.test/products")

        assertEquals("Bearer test-token", authorization())
    }

    @Test
    fun sendsNoTokenToAnotherHost() = runTest {
        clientWith(FakeSessionStore(session), host = "api.example.test").get("https://other.example.test/products")

        assertNull(authorization())
    }
}
