package nl.restpartijen.app.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
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
import kotlin.test.AfterTest
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

    private val clients = mutableListOf<HttpClient>()

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
    ).also { clients += it }

    // The client does not close an engine it was given (manageEngine = false), so close both.
    @AfterTest
    fun closeClients() {
        clients.forEach {
            it.close()
            it.engine.close()
        }
    }

    private fun authorization() = lastRequest?.headers?.get(HttpHeaders.Authorization)

    @Test
    fun readsAGoodResponseAndIgnoresUnknownFields() = runTest {
        val client = clientAnswering(body = """{"id":1,"name":"Cheese","extra":"new"}""")

        val item: Item = client.get("items/1").body()

        assertEquals(Item(id = 1, name = "Cheese"), item)
    }

    // The error body stays readable after the exception, so the conversion to ApiResult (A-22) can read it.
    @Test
    fun throwsOnAClientErrorWithAReadableErrorBody() = runTest {
        val client = clientAnswering(HttpStatusCode.NotFound, """{"code":"NOT_FOUND","message":"Item not found"}""")

        val error = assertFailsWith<ClientRequestException> { client.get("items/9") }

        assertEquals(ErrorResponseDto("NOT_FOUND", "Item not found"), error.response.body<ErrorResponseDto>())
    }

    // Built from ApiConfig on purpose: the test must keep passing when HOST changes for a real phone.
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

    // The API never redirects, so the client must report a redirect as an error instead of following it.
    @Test
    fun doesNotFollowARedirect() = runTest {
        var requests = 0
        val client = createHttpClient(
            MockEngine {
                requests++
                respond(
                    content = "",
                    status = HttpStatusCode.Found,
                    headers = headersOf(HttpHeaders.Location, "https://other.example.test/products"),
                )
            },
            FakeSessionStore(session),
        ).also { clients += it }

        assertFailsWith<RedirectResponseException> { client.get("products") }
        assertEquals(1, requests)
    }

    @Test
    fun sendsNoTokenToAnotherHost() = runTest {
        clientAnswering(store = FakeSessionStore(session)).get("https://other.example.test/products")

        assertNull(authorization())
    }
}
