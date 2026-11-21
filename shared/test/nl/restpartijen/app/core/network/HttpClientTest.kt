package nl.restpartijen.app.core.network

import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import nl.restpartijen.app.core.model.Role
import nl.restpartijen.app.core.model.Session
import nl.restpartijen.app.core.session.FakeSessionStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class HttpClientTest {
    // Test-only body; the real DTOs come with the repositories.
    @Serializable
    private data class Item(val id: Long, val name: String)

    private val session = Session(token = "test-token", role = Role.COLLECTOR, supplierId = null)

    // The last request that reached the engine; the tests look at its URL and headers.
    private var lastRequest: HttpRequestData? = null

    private fun clientAnswering(
        status: HttpStatusCode = HttpStatusCode.OK,
        body: String = "{}",
        store: FakeSessionStore = FakeSessionStore(),
    ) = createHttpClient(
        MockEngine { request ->
            lastRequest = request
            respond(
                content = body,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        },
        store,
    )

    private fun authorization() = lastRequest?.headers?.get(HttpHeaders.Authorization)

    @Test
    fun readsAGoodResponseAndIgnoresUnknownFields() = runTest {
        val client = clientAnswering(body = """{"id":1,"name":"Kaas","extra":"nieuw"}""")

        val item: Item = client.get("items/1").body()

        assertEquals(Item(id = 1, name = "Kaas"), item)
    }

    @Test
    fun throwsOnAClientError() = runTest {
        val client = clientAnswering(HttpStatusCode.NotFound, """{"code":"NOT_FOUND","message":"x"}""")

        assertFailsWith<ClientRequestException> { client.get("items/9") }
    }

    @Test
    fun sendsARelativePathToTheApiBaseUrl() = runTest {
        clientAnswering().get("products")

        assertEquals("${ApiConfig.BASE_URL}products", lastRequest?.url.toString())
    }

    // SessionAuthorization itself is tested in SessionAuthorizationTest; these two check that it is installed
    // with the API host as allowedHost.
    @Test
    fun sendsTheTokenToTheApi() = runTest {
        clientAnswering(store = FakeSessionStore(session)).get("products")

        assertEquals("Bearer test-token", authorization())
    }

    @Test
    fun sendsNoTokenToAnotherHost() = runTest {
        clientAnswering(store = FakeSessionStore(session)).get("https://other.example.test/products")

        assertNull(authorization())
    }
}
