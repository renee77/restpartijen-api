package nl.restpartijen.app.core.network

import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HttpClientTest {
    // Test-only body; the real DTOs come with the repositories.
    @Serializable
    private data class Item(val id: Long, val name: String)

    private fun clientAnswering(status: HttpStatusCode, body: String) = createHttpClient(
        MockEngine {
            respond(
                content = body,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        },
    )

    @Test
    fun readsAGoodResponseAndIgnoresUnknownFields() = runTest {
        val client = clientAnswering(HttpStatusCode.OK, """{"id":1,"name":"Kaas","extra":"nieuw"}""")

        val item: Item = client.get("https://api.example.test/items/1").body()

        assertEquals(Item(id = 1, name = "Kaas"), item)
    }

    @Test
    fun throwsOnAClientError() = runTest {
        val client = clientAnswering(HttpStatusCode.NotFound, """{"code":"NOT_FOUND","message":"x"}""")

        assertFailsWith<ClientRequestException> { client.get("https://api.example.test/items/9") }
    }
}
